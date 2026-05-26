package com.example.demo.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final OvertimeRepository overtimeRepository;
    private final WorkerRepository workerRepository;
    private final SiteRepository siteRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVE_WORKERS_KEY = "active_workers";
    private static final int STANDARD_HOURS = 8;
    private static final int MAX_HOURS = 16;
    private static final int MONTHLY_OT_CAP = 60;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             OvertimeRepository overtimeRepository,
                             WorkerRepository workerRepository,
                             SiteRepository siteRepository,
                             RedisTemplate<String, Object> redisTemplate) {
        this.attendanceRepository = attendanceRepository;
        this.overtimeRepository = overtimeRepository;
        this.workerRepository = workerRepository;
        this.siteRepository = siteRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public AttendanceLog clockIn(Long workerId, Long siteId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("WORKER_NOT_FOUND:Worker not found"));

        if (!worker.getActive())
            throw new RuntimeException("WORKER_INACTIVE:Worker is not active");

        if (LocalDateTime.now().isBefore(LocalDateTime.now().minusSeconds(1)))
            throw new RuntimeException("INVALID_CLOCKIN:Clock-in time cannot be in the future");

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("SITE_NOT_FOUND:Site not found"));

        if (!site.getActive())
            throw new RuntimeException("SITE_INACTIVE:Site is not active");

        AttendanceLog existing = attendanceRepository.findActiveAttendance(workerId);
        if (existing != null)
            throw new RuntimeException("DUPLICATE_CLOCK_IN:Worker is already clocked in at Site: " + existing.getSite().getSiteName());

        AttendanceLog log = new AttendanceLog();
        log.setWorker(worker);
        log.setSite(site);
        log.setClockIn(LocalDateTime.now());
        attendanceRepository.save(log);

        // Add to Redis cache
        Map<String, String> workerInfo = new HashMap<>();
        workerInfo.put("workerId", workerId.toString());
        workerInfo.put("workerName", worker.getName());
        workerInfo.put("siteId", siteId.toString());
        workerInfo.put("siteName", site.getSiteName());
        workerInfo.put("clockIn", log.getClockIn().toString());

        redisTemplate.opsForHash().putAll(ACTIVE_WORKERS_KEY + ":" + workerId, workerInfo);
        redisTemplate.expire(ACTIVE_WORKERS_KEY + ":" + workerId, Duration.ofHours(MAX_HOURS));

        return log;
    }

    @Transactional
    public AttendanceLog clockOut(Long workerId) {
        workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("WORKER_NOT_FOUND:Worker not found"));

        AttendanceLog log = attendanceRepository.findActiveAttendance(workerId);
        if (log == null)
            throw new RuntimeException("NOT_CLOCKED_IN:Worker is not currently clocked in");

        LocalDateTime clockOut = LocalDateTime.now();
        log.setClockOut(clockOut);

        double totalHours = Duration.between(log.getClockIn(), clockOut).toMinutes() / 60.0;
        log.setTotalHours(totalHours);

        if (totalHours > MAX_HOURS) log.setFlagged(true);

        // Calculate overtime
        if (totalHours > STANDARD_HOURS) {
            double overtimeHours = totalHours - STANDARD_HOURS;

            // Check monthly cap
            Double usedOT = attendanceRepository.getTotalOvertimeHoursForMonth(
                    workerId, clockOut.getMonthValue(), clockOut.getYear());
            double remainingCap = MONTHLY_OT_CAP - usedOT;

            if (remainingCap > 0) {
                double cappedOT = Math.min(overtimeHours, remainingCap);
                log.setOvertimeHours(cappedOT);

                // Calculate amount: 1.5x for first 2hrs, 2x beyond
                double hourlyRate = log.getWorker().getDailyWageRate() / STANDARD_HOURS;
                double amount;
                if (cappedOT <= 2) {
                    amount = cappedOT * hourlyRate * 1.5;
                } else {
                    amount = (2 * hourlyRate * 1.5) + ((cappedOT - 2) * hourlyRate * 2);
                }

                OvertimeEntry ot = new OvertimeEntry();
                ot.setWorker(log.getWorker());
                ot.setAttendance(log);
                ot.setDate(clockOut.toLocalDate());
                ot.setOvertimeHours(cappedOT);
                ot.setOvertimeRateApplied(cappedOT <= 2 ? 1.5 : 2.0);
                ot.setAmount(amount);
                overtimeRepository.save(ot);
            }
        }

        attendanceRepository.save(log);

        // Remove from Redis
        redisTemplate.delete(ACTIVE_WORKERS_KEY + ":" + workerId);

        return log;
    }

    public Object getActiveWorkers() {
        try {
            Set<String> keys = redisTemplate.keys(ACTIVE_WORKERS_KEY + ":*");
            return keys != null ? keys : new java.util.HashSet<>();
        } catch (Exception e) {
            // Redis is down - fallback to DB
            return attendanceRepository.findAll()
                    .stream()
                    .filter(a -> a.getClockOut() == null)
                    .map(a -> "active_workers:" + a.getWorker().getId())
                    .collect(java.util.stream.Collectors.toSet());
        }
    }

    public Page<AttendanceLog> getAttendanceLog(Long workerId, LocalDateTime from,
                                                LocalDateTime to, int page, int size) {
        return attendanceRepository.findByWorkerAndDateRange(
                workerId, from, to, PageRequest.of(page, size));
    }
}