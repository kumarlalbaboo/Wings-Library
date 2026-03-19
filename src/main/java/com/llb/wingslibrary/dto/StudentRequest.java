package com.llb.wingslibrary.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StudentRequest {

    @NotBlank(message = "Student code is required")
    private String studentCode;

    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    @NotBlank
    private String mobile;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Seat ID is required")
    private Long seatId;

    private LocalDateTime admissionDate;


}