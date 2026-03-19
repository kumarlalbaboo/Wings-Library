package com.llb.wingslibrary.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {

    private Long id;
    private String studentCode;
    private String name;
    private String mobile;
    private String address;
    private String seatNumber;
    private LocalDate admissionDate;
    private LocalDate paidDate;
}