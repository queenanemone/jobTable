package com.jobtable.controller;

import com.jobtable.dto.workflow.JobWorkflowRequest;
import com.jobtable.dto.workflow.JobWorkflowResponse;
import com.jobtable.service.JobWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Workflow", description = "직업 간 작업 흐름 API")
@RestController
@RequestMapping("/api/workflows")
public class JobWorkflowController {

    private final JobWorkflowService workflowService;

    public JobWorkflowController(JobWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Operation(summary = "전체 워크플로우 조회")
    @GetMapping
    public ResponseEntity<List<JobWorkflowResponse>> getAllWorkflows(
            @RequestParam(required = false) Integer fromJobId,
            @RequestParam(required = false) Integer toJobId) {

        if (fromJobId != null) return ResponseEntity.ok(workflowService.getWorkflowsByFromJob(fromJobId));
        if (toJobId != null)   return ResponseEntity.ok(workflowService.getWorkflowsByToJob(toJobId));
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }

    @Operation(summary = "워크플로우 생성")
    @PostMapping
    public ResponseEntity<JobWorkflowResponse> createWorkflow(@Valid @RequestBody JobWorkflowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowService.createWorkflow(request));
    }

    @Operation(summary = "워크플로우 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Integer id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }
}
