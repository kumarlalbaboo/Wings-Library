package com.llb.wingslibrary.service;

import com.llb.wingslibrary.dto.SeatRequest;
import com.llb.wingslibrary.dto.SeatResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SeatService {

    SeatResponse create(SeatRequest request);

    Page<SeatResponse> getAll(int page, int size);

    List<SeatResponse> getAvailableSeats();

    SeatResponse update(Long id, SeatRequest request);

    void delete(Long id);
}