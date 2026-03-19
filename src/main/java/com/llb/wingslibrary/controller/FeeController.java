package com.llb.wingslibrary.controller;

import com.llb.wingslibrary.dto.FeeDashboardResponse;
import com.llb.wingslibrary.dto.FeeResponse;
import com.llb.wingslibrary.dto.MonthlyDashboardResponse;
import com.llb.wingslibrary.entity.Fee;
import com.llb.wingslibrary.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

@RestController
@RequestMapping("/fees")
@RequiredArgsConstructor
public class FeeController {

    @Autowired
    private final FeeService feeService;

    @GetMapping("/dashboard")
    public FeeDashboardResponse getDashboard() {
        return feeService.getDashboard();
    }

    @PutMapping("/pay/{id}")
    public ResponseEntity<Fee> pay(@PathVariable Long id) {
        return ResponseEntity.ok(feeService.payFee(id));
    }

    @GetMapping("/dashboard/monthly")
    public MonthlyDashboardResponse monthly(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return feeService.getMonthlyDashboard(month, year);
    }

    @GetMapping("/dashboard/yearly")
    public MonthlyDashboardResponse yearly(
            @RequestParam Integer year) {
        return feeService.getYearlyDashboard(year);
    }

    @GetMapping("/student/{studentId}")
    public Page<FeeResponse> history(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return feeService.getStudentFeeHistory(studentId, page, size);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() throws IOException {

        byte[] data = feeService.exportFeesToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=fees.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

}