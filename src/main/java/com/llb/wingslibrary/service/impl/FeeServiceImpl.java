package com.llb.wingslibrary.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.llb.wingslibrary.dto.FeeDashboardResponse;
import com.llb.wingslibrary.dto.FeeResponse;
import com.llb.wingslibrary.dto.MonthlyDashboardResponse;
import com.llb.wingslibrary.entity.Fee;
import com.llb.wingslibrary.entity.FeeStatus;
import com.llb.wingslibrary.entity.PaymentHistory;
import com.llb.wingslibrary.repository.FeeRepository;
import com.llb.wingslibrary.repository.PaymentHistoryRepository;
import com.llb.wingslibrary.repository.StudentRepository;
import com.llb.wingslibrary.service.FeeService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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

    private final PaymentHistoryRepository paymentHistoryRepository;

    @Override
    public Fee payFee(Long feeId) {

        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee not found"));

        if (fee.getStatus() == FeeStatus.PAID) {
            throw new RuntimeException("Fee already paid");
        }

        fee.setStatus(FeeStatus.PAID);
        fee.setPaidDate(LocalDate.now());

        // SAVE PAYMENT HISTORY
        PaymentHistory history = new PaymentHistory();
        history.setStudent(fee.getStudent());
        history.setAmount(fee.getAmount());
        history.setMonth(fee.getMonth());
        history.setYear(fee.getYear());
        history.setPaidDate(LocalDate.now());

        paymentHistoryRepository.save(history);

        return feeRepository.save(fee);
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdue() {

        List<Fee> fees = feeRepository.findAll();

        LocalDate today = LocalDate.now();

        for (Fee fee : fees) {

            if (fee.getStatus() == FeeStatus.PAID) continue;

            if (fee.getDueDate() != null && fee.getDueDate().isBefore(today)) {
                fee.setStatus(FeeStatus.OVERDUE);
            }
        }

        feeRepository.saveAll(fees);
    }

    @Override
    @Scheduled(cron = "0 5 0 * * ?")
    public void generateMonthlyFees() {

        LocalDate today = LocalDate.now();

        int day = today.getDayOfMonth();
        int month = today.getMonthValue();
        int year = today.getYear();

        studentRepository.findAll()
                .forEach(student -> {

                    if (student.isDeleted()) return;
                    if (student.getAdmissionDate() == null) return; // null safety

                    // ✅ EDGE CASE HANDLING (31st / Feb)
                    int admissionDay = student.getAdmissionDate().getDayOfMonth();
                    int lastDay = today.lengthOfMonth();

                    if (admissionDay > lastDay) {
                        admissionDay = lastDay;
                    }

                    if (admissionDay != day) return;

                    boolean exists = feeRepository
                            .existsByStudentIdAndMonthAndYear(
                                    student.getId(), month, year);

                    if (!exists) {
                        Fee fee = new Fee();
                        fee.setStudent(student);
                        fee.setMonth(month);
                        fee.setYear(year);
                        fee.setAmount(new BigDecimal("1000"));
                        fee.setStatus(FeeStatus.UNPAID);

                        // ✅ USE COMMON METHOD
                        fee.setDueDate(calculateDueDate(today));

                        feeRepository.save(fee);
                    }
                });
    }

    private LocalDate calculateDueDate(LocalDate baseDate) {
        return baseDate.plusDays(1);
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
                        .paidDate(f.getPaidDate()) // ✅ added
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

    @Override
    public byte[] exportFeesToExcel1() throws IOException {

        List<Fee> fees = feeRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Fees");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Student ID");
        header.createCell(1).setCellValue("Month");
        header.createCell(2).setCellValue("Year");
        header.createCell(3).setCellValue("Amount");
        header.createCell(4).setCellValue("Status");

        int rowNum = 1;

        for (Fee fee : fees) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(fee.getStudent().getId());
            row.createCell(1).setCellValue(fee.getMonth());
            row.createCell(2).setCellValue(fee.getYear());
            row.createCell(3).setCellValue(fee.getAmount().doubleValue());
            row.createCell(4).setCellValue(fee.getStatus().name());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    @Override
    public byte[] exportFeesToPdf() throws Exception {

        List<Fee> fees = feeRepository.findAll();

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        PdfPTable table = new PdfPTable(5);

        // Header
        table.addCell("Student ID");
        table.addCell("Month");
        table.addCell("Year");
        table.addCell("Amount");
        table.addCell("Status");

        // Data
        for (Fee fee : fees) {
            table.addCell(String.valueOf(fee.getStudent().getId()));
            table.addCell(String.valueOf(fee.getMonth()));
            table.addCell(String.valueOf(fee.getYear()));
            table.addCell(fee.getAmount().toString());
            table.addCell(fee.getStatus().name());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

}