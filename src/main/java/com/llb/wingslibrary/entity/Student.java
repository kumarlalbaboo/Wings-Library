package com.llb.wingslibrary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_student_code", columnList = "studentCode")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String studentCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mobile;

    private String address;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] idProof;

    private String photoContentType;
    private String idProofContentType;

    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Fee> fees = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "seat_id", unique = true)
    @JsonManagedReference
    private Seat seat;

}
