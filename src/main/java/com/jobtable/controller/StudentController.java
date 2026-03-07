package com.jobtable.controller;

import com.jobtable.dto.student.StudentJobRequest;
import com.jobtable.dto.student.StudentRequest;
import com.jobtable.dto.student.StudentResponse;
import com.jobtable.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student", description = "학생 관리 API")
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "학생 목록 조회")
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @Operation(summary = "학생 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @Operation(summary = "학생 생성")
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @Operation(summary = "학생 직업 변경", description = "jobId를 null로 보내면 직업 해제")
    @PatchMapping("/{id}/job")
    public ResponseEntity<StudentResponse> assignJob(@PathVariable Integer id,
                                                      @RequestBody StudentJobRequest request) {
        return ResponseEntity.ok(studentService.assignJob(id, request));
    }
}
