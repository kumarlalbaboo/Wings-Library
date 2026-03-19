package com.llb.wingslibrary.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyDashboardResponse {

    private Integer month;
    private Integer year;
    private long totalFees;
    private Double collectedAmount;
}
