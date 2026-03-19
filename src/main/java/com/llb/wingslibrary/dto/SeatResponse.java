package com.llb.wingslibrary.dto;

import com.llb.wingslibrary.entity.SeatStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponse {

    private Long id;
    private String seatNumber;
    private SeatStatus status;
}
