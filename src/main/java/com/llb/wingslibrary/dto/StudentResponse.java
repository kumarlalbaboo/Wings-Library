package com.llb.wingslibrary.dto;

import lombok.*;

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
}