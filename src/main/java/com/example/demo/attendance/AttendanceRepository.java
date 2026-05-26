package com.example.demo.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceLog, Long> {

    @Query(value = "SELECT a FROM AttendanceLog a WHERE a.worker.id = :workerId AND a.clockIn >= :from AND a.clockIn <= :to",
            countQuery = "SELECT count(a) FROM AttendanceLog a WHERE a.worker.id = :workerId AND a.clockIn >= :from AND a.clockIn <= :to")
    Page<AttendanceLog> findByWorkerAndDateRange(
            @Param("workerId") Long workerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("SELECT a FROM AttendanceLog a WHERE a.worker.id = :workerId AND a.clockOut IS NULL")
    AttendanceLog findActiveAttendance(@Param("workerId") Long workerId);

    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM AttendanceLog a " +
            "WHERE a.worker.id = :workerId AND MONTH(a.clockIn) = :month AND YEAR(a.clockIn) = :year")
    Double getTotalOvertimeHoursForMonth(
            @Param("workerId") Long workerId,
            @Param("month") int month,
            @Param("year") int year);
}