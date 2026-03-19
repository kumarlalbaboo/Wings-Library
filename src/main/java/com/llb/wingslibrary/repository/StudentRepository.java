package com.llb.wingslibrary.repository;

import com.llb.wingslibrary.entity.FeeStatus;
import com.llb.wingslibrary.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentCode(String code);

    long count();

    Optional<Student> findByIdAndDeletedFalse(Long id);

    Page<Student> findByDeletedFalse(Pageable pageable);

    long countByDeletedFalse();

    List<Student> findByNameContainingIgnoreCase(String name);

    @Query("""
        SELECT DISTINCT s
        FROM Student s
        JOIN s.fees f
        WHERE f.status = :status
    """)
    List<Student> findByFeeStatus(FeeStatus status);

}

