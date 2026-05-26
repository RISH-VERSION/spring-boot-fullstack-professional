package com.example.demo.attendance;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody Map<String, Long> request) {
        Long workerId = request.get("workerId");
        Long siteId = request.get("siteId");
        AttendanceLog log = attendanceService.clockIn(workerId, siteId);
        return ResponseEntity.ok(Map.of(
                "message", "Clocked in successfully",
                "attendanceId", log.getId(),
                "clockIn", log.getClockIn().toString()
        ));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody Map<String, Long> request) {
        Long workerId = request.get("workerId");
        AttendanceLog log = attendanceService.clockOut(workerId);
        return ResponseEntity.ok(Map.of(
                "message", "Clocked out successfully",
                "totalHours", log.getTotalHours(),
                "overtimeHours", log.getOvertimeHours(),
                "flagged", log.getFlagged()
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveWorkers() {
        Set<String> activeWorkers = attendanceService.getActiveWorkers();
        return ResponseEntity.ok(Map.of(
                "activeWorkers", activeWorkers,
                "count", activeWorkers != null ? activeWorkers.size() : 0
        ));
    }

    @GetMapping("/log")
    public ResponseEntity<?> getAttendanceLog(
            @RequestParam Long workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AttendanceLog> logs = attendanceService.getAttendanceLog(workerId, from, to, page, size);
        return ResponseEntity.ok(Map.of(
                "content", logs.getContent(),
                "totalElements", logs.getTotalElements(),
                "totalPages", logs.getTotalPages(),
                "currentPage", logs.getNumber()
        ));
    }
}