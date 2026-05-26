package com.example.demo.attendance;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class OvertimeService {

    private final OvertimeRepository overtimeRepository;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OvertimeService(OvertimeRepository overtimeRepository,
                           WorkerRepository workerRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.overtimeRepository = overtimeRepository;
        this.workerRepository = workerRepository;
        this.eventPublisher = eventPublisher;
    }

    public OvertimeSummary getOvertimeSummary(Long workerId, String month) {
        YearMonth ym = YearMonth.parse(month);
        List<OvertimeEntry> entries = overtimeRepository.findByWorkerAndMonth(
                workerId, ym.getMonthValue(), ym.getYear());

        double totalHours = entries.stream().mapToDouble(OvertimeEntry::getOvertimeHours).sum();
        double totalAmount = entries.stream().mapToDouble(OvertimeEntry::getAmount).sum();

        return new OvertimeSummary(workerId, month, totalHours, totalAmount, entries);
    }

    @Transactional
    public double settleOvertime(Long workerId, String month) {
        YearMonth ym = YearMonth.parse(month);

        // Cannot settle current month
        if (ym.equals(YearMonth.now()))
            throw new RuntimeException("INVALID_MONTH:Cannot settle current month");

        workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("WORKER_NOT_FOUND:Worker not found"));

        List<OvertimeEntry> entries = overtimeRepository.findByWorkerAndMonth(
                workerId, ym.getMonthValue(), ym.getYear());

        if (entries.isEmpty())
            throw new RuntimeException("NO_ENTRIES:No overtime entries found for this month");

        boolean alreadySettled = entries.stream()
                .allMatch(e -> e.getSettlementStatus() == OvertimeEntry.SettlementStatus.SETTLED);
        if (alreadySettled)
            throw new RuntimeException("ALREADY_SETTLED:Overtime already settled for this month");

        double totalAmount = entries.stream().mapToDouble(OvertimeEntry::getAmount).sum();

        for (OvertimeEntry entry : entries) {
            entry.setSettlementStatus(OvertimeEntry.SettlementStatus.SETTLED);
        }
        overtimeRepository.saveAll(entries);

        // Publish event AFTER transaction commits
        eventPublisher.publishEvent(new OvertimeSettledEvent(this, workerId, month, totalAmount));

        return totalAmount;
    }

    // Inner classes
    public static class OvertimeSummary {
        public Long workerId;
        public String month;
        public double totalOvertimeHours;
        public double totalAmount;
        public List<OvertimeEntry> breakdown;

        public OvertimeSummary(Long workerId, String month, double totalOvertimeHours,
                               double totalAmount, List<OvertimeEntry> breakdown) {
            this.workerId = workerId;
            this.month = month;
            this.totalOvertimeHours = totalOvertimeHours;
            this.totalAmount = totalAmount;
            this.breakdown = breakdown;
        }
    }

    public static class OvertimeSettledEvent extends org.springframework.context.ApplicationEvent {
        public final Long workerId;
        public final String month;
        public final double totalAmount;

        public OvertimeSettledEvent(Object source, Long workerId, String month, double totalAmount) {
            super(source);
            this.workerId = workerId;
            this.month = month;
            this.totalAmount = totalAmount;
        }
    }
}