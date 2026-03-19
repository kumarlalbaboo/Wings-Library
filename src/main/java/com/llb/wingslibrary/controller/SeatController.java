package com.llb.wingslibrary.controller;

import com.llb.wingslibrary.dto.*;
import com.llb.wingslibrary.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/addSeatNumber")
    public SeatResponse create(@Valid @RequestBody SeatRequest request) {
        return seatService.create(request);
    }

    @GetMapping
    public Page<SeatResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return seatService.getAll(page, size);
    }

    @GetMapping("/available")
    public List<SeatResponse> getAvailableSeats() {
        return seatService.getAvailableSeats();
    }

    @PutMapping("/{id}")
    public SeatResponse update(@PathVariable Long id,
                               @Valid @RequestBody SeatRequest request) {
        return seatService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        seatService.delete(id);
    }
}