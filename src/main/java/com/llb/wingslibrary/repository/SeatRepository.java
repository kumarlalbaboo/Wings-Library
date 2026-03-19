package com.llb.wingslibrary.repository;

import com.llb.wingslibrary.entity.Seat;
import com.llb.wingslibrary.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    boolean existsBySeatNumber(String seatNumber);

    List<Seat> findByStatus(SeatStatus status);

    Optional<Seat> findByIdAndStatus(Long id, SeatStatus status);

}