package com.llb.wingslibrary.service.impl;

import com.llb.wingslibrary.dto.*;
import com.llb.wingslibrary.entity.*;
import com.llb.wingslibrary.exception.ResourceNotFoundException;
import com.llb.wingslibrary.repository.SeatRepository;
import com.llb.wingslibrary.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    @Override
    public SeatResponse create(SeatRequest request) {

        if (seatRepository.existsBySeatNumber(request.getSeatNumber())) {
            throw new RuntimeException("Seat number already exists");
        }

        Seat seat = new Seat();
        seat.setSeatNumber(request.getSeatNumber());
        seat.setStatus(SeatStatus.AVAILABLE);

        return mapToResponse(seatRepository.save(seat));
    }

    @Override
    public Page<SeatResponse> getAll(int page, int size) {

        Page<Seat> seats =
                seatRepository.findAll(PageRequest.of(page, size));

        return seats.map(this::mapToResponse);
    }

    @Override
    public List<SeatResponse> getAvailableSeats() {

        return seatRepository.findByStatus(SeatStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SeatResponse update(Long id, SeatRequest request) {

        Seat seat = seatRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Seat not found"));

        if (!seat.getSeatNumber().equals(request.getSeatNumber())
                && seatRepository.existsBySeatNumber(request.getSeatNumber())) {
            throw new RuntimeException("Seat number already exists");
        }

        seat.setSeatNumber(request.getSeatNumber());

        return mapToResponse(seatRepository.save(seat));
    }

    @Override
    public void delete(Long id) {

        Seat seat = seatRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Seat not found"));

        if (seat.getStatus() == SeatStatus.OCCUPIED) {
            throw new RuntimeException("Cannot delete occupied seat");
        }

        seatRepository.delete(seat);
    }

    private SeatResponse mapToResponse(Seat seat) {

        return SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .status(seat.getStatus())
                .build();
    }
}