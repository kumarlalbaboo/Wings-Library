package com.llb.wingslibrary.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeResponse {

    private Long id;
    private Integer month;
    private Integer year;
    private BigDecimal amount;
    private String status;
}