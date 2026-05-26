package com.example.demo.attendance;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "overtime_entries", indexes = {
        @Index(name = "idx_overtime_worker", columnList = "worker_id"),
        @Index(name = "idx_overtime_date", columnList = "date")
})
public class OvertimeEntry {

    @Id
    @SequenceGenerator(name = "overtime_sequence", sequenceName = "overtime_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "overtime_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceLog attendance;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double overtimeHours;

    @Column(nullable = false)
    private Double overtimeRateApplied;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    public enum SettlementStatus {
        PENDING, SETTLED
    }

    public OvertimeEntry() {}

    public Long getId() { return id; }
    public Worker getWorker() { return worker; }
    public AttendanceLog getAttendance() { return attendance; }
    public LocalDate getDate() { return date; }
    public Double getOvertimeHours() { return overtimeHours; }
    public Double getOvertimeRateApplied() { return overtimeRateApplied; }
    public Double getAmount() { return amount; }
    public SettlementStatus getSettlementStatus() { return settlementStatus; }

    public void setId(Long id) { this.id = id; }
    public void setWorker(Worker worker) { this.worker = worker; }
    public void setAttendance(AttendanceLog attendance) { this.attendance = attendance; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setOvertimeRateApplied(Double overtimeRateApplied) { this.overtimeRateApplied = overtimeRateApplied; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setSettlementStatus(SettlementStatus settlementStatus) { this.settlementStatus = settlementStatus; }
}