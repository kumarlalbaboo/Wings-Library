package com.llb.wingslibrary.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatStatus status; // AVAILABLE / OCCUPIED

    @OneToOne(mappedBy = "seat")
    @JsonBackReference
    private Student student;
}