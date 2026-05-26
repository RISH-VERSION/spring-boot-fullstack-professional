package com.example.demo.attendance;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs", indexes = {
        @Index(name = "idx_attendance_worker", columnList = "worker_id"),
        @Index(name = "idx_attendance_clockin", columnList = "clock_in")
})
public class AttendanceLog {

    @Id
    @SequenceGenerator(name = "attendance_sequence", sequenceName = "attendance_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attendance_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "total_hours")
    private Double totalHours;

    @Column(name = "overtime_hours")
    private Double overtimeHours = 0.0;

    @Column(nullable = false)
    private Boolean flagged = false;

    public AttendanceLog() {}

    public Long getId() { return id; }
    public Worker getWorker() { return worker; }
    public Site getSite() { return site; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public Double getTotalHours() { return totalHours; }
    public Double getOvertimeHours() { return overtimeHours; }
    public Boolean getFlagged() { return flagged; }

    public void setId(Long id) { this.id = id; }
    public void setWorker(Worker worker) { this.worker = worker; }
    public void setSite(Site site) { this.site = site; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setFlagged(Boolean flagged) { this.flagged = flagged; }
}