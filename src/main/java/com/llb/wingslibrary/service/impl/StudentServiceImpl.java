package com.llb.wingslibrary.service.impl;

import com.llb.wingslibrary.dto.StudentRequest;
import com.llb.wingslibrary.dto.StudentResponse;
import com.llb.wingslibrary.entity.FeeStatus;
import com.llb.wingslibrary.entity.Seat;
import com.llb.wingslibrary.entity.SeatStatus;
import com.llb.wingslibrary.entity.Student;
import com.llb.wingslibrary.exception.ResourceNotFoundException;
import com.llb.wingslibrary.repository.SeatRepository;
import com.llb.wingslibrary.repository.StudentRepository;
import com.llb.wingslibrary.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    private final SeatRepository seatRepository;

    @Override
    public StudentResponse create(StudentRequest request, MultipartFile photo, MultipartFile idProof) {

        Seat seat = seatRepository
                .findByIdAndStatus(request.getSeatId(), SeatStatus.AVAILABLE)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Seat not available"));

        Student student = new Student();
        student.setStudentCode(request.getStudentCode());
        student.setName(request.getName());
        student.setMobile(request.getMobile());
        student.setAddress(request.getAddress());
        student.setAdmissionDate(LocalDateTime.now());
        student.setSeat(seat);

        // PHOTO
        if (photo != null && !photo.isEmpty()) {
            validateFile(photo, "photo");
            try {
                student.setPhoto(photo.getBytes());
                student.setPhotoContentType(photo.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Failed to store photo");
            }
        }

        // ID PROOF
        if (idProof != null && !idProof.isEmpty()) {
            validateFile(idProof, "idProof");
            try {
                student.setIdProof(idProof.getBytes());
                student.setIdProofContentType(idProof.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Failed to store ID proof");
            }
        }

        seat.setStatus(SeatStatus.OCCUPIED);

        Student saved = studentRepository.save(student);

        return mapToResponse(saved);
    }

    private void validateFile(MultipartFile file, String type) {

        if (file == null || file.isEmpty()) return;

        // Limit size (2MB example)
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException(type + " file size exceeds 2MB limit");
        }

        String contentType = file.getContentType();

        if (type.equals("photo")) {
            if (!"image/jpeg".equals(contentType) &&
                    !"image/jpg".equals(contentType)) {
                throw new RuntimeException("Only JPG images allowed for photo");
            }
        }

        if (type.equals("idProof")) {
            if (!"application/pdf".equals(contentType)) {
                throw new RuntimeException("Only PDF allowed for ID proof");
            }
        }
    }

    @Override
    public Page<StudentResponse> getAll(int page, int size) {

        Page<Student> students = studentRepository.findByDeletedFalse(PageRequest.of(page, size));

        return students.map(this::mapToResponse);
    }

    private StudentResponse mapToResponse(Student student) {

        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .name(student.getName())
                .mobile(student.getMobile())
                .address(student.getAddress())
                .seatNumber(
                        student.getSeat() != null
                                ? student.getSeat().getSeatNumber()
                                : null
                )
                .build();
    }

    @Override
    public StudentResponse getById(Long id) {

        Student student = studentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found with id " + id));

        return mapToResponse(student);
    }

    @Override
    public StudentResponse update(Long id, StudentRequest request) {

        Student student = studentRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found"));

        // Update basic fields
        student.setName(request.getName());
        student.setMobile(request.getMobile());
        student.setAddress(request.getAddress());

        // If seat is changed
        if (request.getSeatId() != null &&
                (student.getSeat() == null ||
                        !student.getSeat().getId().equals(request.getSeatId()))) {

            // Release old seat
            if (student.getSeat() != null) {
                Seat oldSeat = student.getSeat();
                oldSeat.setStatus(SeatStatus.AVAILABLE);
                oldSeat.setStudent(null);
            }

            // Assign new seat
            Seat newSeat = seatRepository
                    .findByIdAndStatus(request.getSeatId(), SeatStatus.AVAILABLE)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Seat not available"));

            newSeat.setStatus(SeatStatus.OCCUPIED);
            newSeat.setStudent(student);
            student.setSeat(newSeat);
        }

        return mapToResponse(student);
    }

    @Override
    public void delete(Long id) {

        Student student = studentRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found"));

        student.setDeleted(true);

        Seat seat = student.getSeat();
        if (seat != null) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setStudent(null);
            student.setSeat(null);
        }
    }

    @Override
    public Student findEntityById(Long id) {
        return studentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found with id " + id));
    }

    @Override
    public List<StudentResponse> searchByName(String name) {

        return studentRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> filterByFeeStatus(String status) {

        FeeStatus feeStatus = FeeStatus.valueOf(status);

        return studentRepository.findByFeeStatus(feeStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

}
