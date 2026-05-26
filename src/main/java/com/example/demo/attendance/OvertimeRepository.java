package com.example.demo.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OvertimeRepository extends JpaRepository<OvertimeEntry, Long> {

    @Query("SELECT o FROM OvertimeEntry o WHERE o.worker.id = :workerId " +
            "AND MONTH(o.date) = :month AND YEAR(o.date) = :year")
    List<OvertimeEntry> findByWorkerAndMonth(
            @Param("workerId") Long workerId,
            @Param("month") int month,
            @Param("year") int year);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM OvertimeEntry o WHERE o.worker.id = :workerId " +
            "AND MONTH(o.date) = :month AND YEAR(o.date) = :year AND o.settlementStatus = 'SETTLED'")
    Double getTotalSettledAmount(
            @Param("workerId") Long workerId,
            @Param("month") int month,
            @Param("year") int year);
}