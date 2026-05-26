package com.example.demo.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {

    private final OvertimeService overtimeService;

    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    @GetMapping("/summary/{workerId}")
    public ResponseEntity<?> getOvertimeSummary(
            @PathVariable Long workerId,
            @RequestParam String month) {
        OvertimeService.OvertimeSummary summary = overtimeService.getOvertimeSummary(workerId, month);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/settle/{workerId}")
    public ResponseEntity<?> settleOvertime(
            @PathVariable Long workerId,
            @RequestParam String month) {
        double totalAmount = overtimeService.settleOvertime(workerId, month);
        return ResponseEntity.ok(Map.of(
                "message", "Overtime settled successfully",
                "workerId", workerId,
                "month", month,
                "totalAmount", totalAmount
        ));
    }
}