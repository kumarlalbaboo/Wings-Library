package com.llb.wingslibrary.controller;

import com.llb.wingslibrary.dto.StudentRequest;
import com.llb.wingslibrary.dto.StudentResponse;
import com.llb.wingslibrary.entity.Student;
import com.llb.wingslibrary.entity.StudentFileType;
import com.llb.wingslibrary.exception.ResourceNotFoundException;
import com.llb.wingslibrary.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static com.llb.wingslibrary.entity.StudentFileType.ID_PROOF;
import static com.llb.wingslibrary.entity.StudentFileType.PHOTO;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping(
            value = "/addStudent",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<StudentResponse> createStudent(

            @RequestParam String studentCode,
            @RequestParam String name,
            @RequestParam String mobile,
            @RequestParam(required = false) String address,
            @RequestParam Long seatId,

            @RequestParam(required = false) MultipartFile photo,
            @RequestParam(required = false) MultipartFile idProof

    ) {

        StudentRequest request = new StudentRequest();
        request.setStudentCode(studentCode);
        request.setName(name);
        request.setMobile(mobile);
        request.setAddress(address);
        request.setSeatId(seatId);

        StudentResponse response =
                studentService.create(request, photo, idProof);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @PathVariable Long id,
            @RequestParam StudentFileType type) {

        Student student = studentService.findEntityById(id);

        byte[] fileData;
        String contentType;
        String fileName;

        switch (type) {

            case PHOTO -> {
                if (student.getPhoto() == null) {
                    throw new ResourceNotFoundException("Photo not found");
                }
                fileData = student.getPhoto();
                contentType = student.getPhotoContentType();
                fileName = "student_photo.jpg";
            }

            case ID_PROOF -> {
                if (student.getIdProof() == null) {
                    throw new ResourceNotFoundException("ID Proof not found");
                }
                fileData = student.getIdProof();
                contentType = student.getIdProofContentType();
                fileName = "student_id_proof.pdf";
            }

            default -> throw new IllegalArgumentException("Invalid file type");
        }

        StreamingResponseBody stream = outputStream -> {
            outputStream.write(fileData);
            outputStream.flush();
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=" + fileName)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(stream);
    }

    @GetMapping
    public ResponseEntity<Page<StudentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                studentService.getAll(page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {

        return ResponseEntity.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
