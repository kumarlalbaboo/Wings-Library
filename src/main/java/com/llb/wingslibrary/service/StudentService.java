package com.llb.wingslibrary.service;

import com.llb.wingslibrary.dto.StudentRequest;
import com.llb.wingslibrary.dto.StudentResponse;
import com.llb.wingslibrary.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {

    public StudentResponse create(StudentRequest request,
                                  MultipartFile photo,
                                  MultipartFile idProof);

    Page<StudentResponse> getAll(int page, int size);

    StudentResponse getById(Long id);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);

    Student findEntityById(Long id);

}