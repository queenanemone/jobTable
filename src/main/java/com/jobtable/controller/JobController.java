package com.jobtable.controller;

import com.jobtable.dto.job.JobRequest;
import com.jobtable.dto.job.JobResponse;
import com.jobtable.dto.jobaction.JobActionRequest;
import com.jobtable.dto.jobaction.JobActionResponse;
import com.jobtable.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Job", description = "직업 관리 API")
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "직업 목록 조회")
    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @Operation(summary = "직업 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Integer id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @Operation(summary = "직업 생성")
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @Operation(summary = "직업 수정")
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Integer id,
                                                  @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @Operation(summary = "직업 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Integer id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "직업별 행위 설정 조회", description = "해당 직업이 수행해야 할 모든 행위 스키마를 반환. 클라이언트는 이를 기반으로 입력 폼을 렌더링.")
    @GetMapping("/{id}/actions")
    public ResponseEntity<List<JobActionResponse>> getJobActions(@PathVariable Integer id) {
        return ResponseEntity.ok(jobService.getJobActions(id));
    }

    @Operation(summary = "직업에 행위 연결")
    @PostMapping("/{id}/actions")
    public ResponseEntity<JobActionResponse> addJobAction(@PathVariable Integer id,
                                                           @Valid @RequestBody JobActionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.addJobAction(id, request));
    }
}
