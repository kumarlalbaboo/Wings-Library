package com.llb.wingslibrary.service.impl;

import com.llb.wingslibrary.dto.FeeDashboardResponse;
import com.llb.wingslibrary.dto.FeeResponse;
import com.llb.wingslibrary.dto.MonthlyDashboardResponse;
import com.llb.wingslibrary.entity.Fee;
import com.llb.wingslibrary.entity.FeeStatus;
import com.llb.wingslibrary.repository.FeeRepository;
import com.llb.wingslibrary.repository.StudentRepository;
import com.llb.wingslibrary.service.FeeService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
@EnableScheduling
public class FeeServiceImpl implements FeeService {

    private final FeeRepository feeRepository;

    private final StudentRepository studentRepository;

    public Fee payFee(Long feeId) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow();
        fee.setStatus(FeeStatus.PAID);
        fee.setPaidDate(LocalDate.now());
        return feeRepository.save(fee);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdue() {
        List<Fee> unpaid = feeRepository.findByStatus(FeeStatus.UNPAID);
        unpaid.forEach(fee -> {
            if (fee.getDueDate().isBefore(LocalDate.now())) {
                fee.setStatus(FeeStatus.OVERDUE);
            }
        });
        feeRepository.saveAll(unpaid);
    }

    @Override
    public FeeDashboardResponse getDashboard() {

        long totalStudents = studentRepository.countByDeletedFalse();
        long totalFees = feeRepository.count();

        long paid = feeRepository.countByStatus(FeeStatus.PAID);
        long unpaid = feeRepository.countByStatus(FeeStatus.UNPAID);
        long overdue = feeRepository.countByStatus(FeeStatus.OVERDUE);

        Double collected = feeRepository.getTotalCollected();
        Double pending = feeRepository.getTotalPending();

        return FeeDashboardResponse.builder()
                .totalStudents(totalStudents)
                .totalFees(totalFees)
                .paidCount(paid)
                .unpaidCount(unpaid)
                .overdueCount(overdue)
                .totalCollected(collected)
                .totalPending(pending)
                .build();
    }

    @Override
    public MonthlyDashboardResponse getMonthlyDashboard(Integer month, Integer year) {

        long total = feeRepository.countByMonthAndYear(month, year);
        Double collected = feeRepository.getCollectedByMonth(month, year);

        return MonthlyDashboardResponse.builder()
                .month(month)
                .year(year)
                .totalFees(total)
                .collectedAmount(collected)
                .build();
    }

    @Override
    public MonthlyDashboardResponse getYearlyDashboard(Integer year) {

        long total = feeRepository.countByYear(year);
        Double collected = feeRepository.getCollectedByYear(year);

        return MonthlyDashboardResponse.builder()
                .year(year)
                .totalFees(total)
                .collectedAmount(collected)
                .build();
    }

    @Override
    public Page<FeeResponse> getStudentFeeHistory(
            Long studentId, int page, int size) {

        Page<Fee> fees =
                feeRepository.findByStudentId(
                        studentId,
                        PageRequest.of(page, size, Sort.by("year")
                                .descending()
                                .and(Sort.by("month").descending()))
                );

        return fees.map(f ->
                FeeResponse.builder()
                        .id(f.getId())
                        .month(f.getMonth())
                        .year(f.getYear())
                        .amount(f.getAmount())
                        .status(f.getStatus().name())
                        .build()
        );
    }

    @Override
    public byte[] exportFeesToExcel() {

        List<Fee> fees = feeRepository.findAll();

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Student ID,Month,Year,Amount,Status\n");

        // Data
        for (Fee fee : fees) {
            sb.append(fee.getStudent().getId()).append(",");
            sb.append(fee.getMonth()).append(",");
            sb.append(fee.getYear()).append(",");
            sb.append(fee.getAmount()).append(",");
            sb.append(fee.getStatus()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

}