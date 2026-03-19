package com.llb.wingslibrary.service;

import com.llb.wingslibrary.dto.FeeDashboardResponse;
import com.llb.wingslibrary.dto.FeeResponse;
import com.llb.wingslibrary.dto.MonthlyDashboardResponse;
import com.llb.wingslibrary.entity.Fee;
import org.springframework.data.domain.Page;

import java.io.IOException;

public interface FeeService {

    public Fee payFee(Long feeId);

    public void markOverdue();

    FeeDashboardResponse getDashboard();

    MonthlyDashboardResponse getMonthlyDashboard(Integer month, Integer year);

    MonthlyDashboardResponse getYearlyDashboard(Integer year);

    Page<FeeResponse> getStudentFeeHistory(Long studentId, int page, int size);

    byte[] exportFeesToExcel() throws IOException;

}