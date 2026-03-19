package com.llb.wingslibrary.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeDashboardResponse {

    private long totalStudents;
    private long totalFees;

    private long paidCount;
    private long unpaidCount;
    private long overdueCount;

    private Double totalCollected;
    private Double totalPending;
}