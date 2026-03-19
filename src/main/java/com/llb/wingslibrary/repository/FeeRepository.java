package com.llb.wingslibrary.repository;

import com.llb.wingslibrary.entity.Fee;
import com.llb.wingslibrary.entity.FeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStatus(FeeStatus status);

    long countByStatus(FeeStatus status);

    @Query("SELECT COALESCE(SUM(f.amount),0) FROM Fee f WHERE f.status = 'PAID'")
    Double getTotalCollected();

    @Query("SELECT COALESCE(SUM(f.amount),0) FROM Fee f WHERE f.status IN ('UNPAID','OVERDUE')")
    Double getTotalPending();

    long countByMonthAndYear(Integer month, Integer year);

    long countByYear(Integer year);

    @Query("""
        SELECT COALESCE(SUM(f.amount),0)
        FROM Fee f
        WHERE f.month = :month AND f.year = :year
        AND f.status = 'PAID'
        """)
    Double getCollectedByMonth(Integer month, Integer year);

    @Query("""
        SELECT COALESCE(SUM(f.amount),0)
        FROM Fee f
        WHERE f.year = :year
        AND f.status = 'PAID'
        """)
    Double getCollectedByYear(Integer year);

    Page<Fee> findByStudentId(Long studentId, Pageable pageable);
}
