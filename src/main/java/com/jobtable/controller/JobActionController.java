package com.jobtable.controller;

import com.jobtable.dto.jobaction.JobActionResponse;
import com.jobtable.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "JobAction", description = "직업-행위 직접 조회 API")
@RestController
@RequestMapping("/api/job-actions")
public class JobActionController {

    private final JobService jobService;

    public JobActionController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "직업-행위 단건 조회", description = "jobActionId로 actionConfig(필드 스키마) 조회. 활동 로그 입력 폼 렌더링에 사용.")
    @GetMapping("/{id}")
    public ResponseEntity<JobActionResponse> getJobAction(@PathVariable Integer id) {
        return ResponseEntity.ok(jobService.getJobAction(id));
    }

    @Operation(summary = "직업-행위 연결 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobAction(@PathVariable Integer id) {
        jobService.deleteJobAction(id);
        return ResponseEntity.noContent().build();
    }
}
