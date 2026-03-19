package com.llb.wingslibrary.repository;

import com.llb.wingslibrary.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByStudentId(Long studentId);
}