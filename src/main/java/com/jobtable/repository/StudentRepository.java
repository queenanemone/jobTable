package com.jobtable.repository;

import com.jobtable.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByCurrentJobId(Integer jobId);
}
