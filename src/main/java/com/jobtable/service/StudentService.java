package com.jobtable.service;

import com.jobtable.dto.student.StudentJobRequest;
import com.jobtable.dto.student.StudentRequest;
import com.jobtable.dto.student.StudentResponse;
import com.jobtable.entity.JobTemplate;
import com.jobtable.entity.Student;
import com.jobtable.repository.JobTemplateRepository;
import com.jobtable.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final JobTemplateRepository jobRepository;

    public StudentService(StudentRepository studentRepository,
                          JobTemplateRepository jobRepository) {
        this.studentRepository = studentRepository;
        this.jobRepository = jobRepository;
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::from)
                .toList();
    }

    public StudentResponse getStudent(Integer id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다. id=" + id));
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        Student student = new Student();
        student.setName(request.getName());
        student.setBalance(request.getBalance() != null ? request.getBalance() : 0);

        if (request.getCurrentJobId() != null) {
            JobTemplate job = jobRepository.findById(request.getCurrentJobId())
                    .orElseThrow(() -> new RuntimeException("직업을 찾을 수 없습니다. id=" + request.getCurrentJobId()));
            student.setCurrentJob(job);
        }
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse assignJob(Integer studentId, StudentJobRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다. id=" + studentId));

        if (request.getJobId() == null) {
            student.setCurrentJob(null);
        } else {
            JobTemplate job = jobRepository.findById(request.getJobId())
                    .orElseThrow(() -> new RuntimeException("직업을 찾을 수 없습니다. id=" + request.getJobId()));
            student.setCurrentJob(job);
        }
        return StudentResponse.from(student);
    }
}
