package com.jobtable.controller;

import com.jobtable.dto.activitylog.ActivityLogRequest;
import com.jobtable.dto.activitylog.ActivityLogResponse;
import com.jobtable.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ActivityLog", description = "통합 활동 로그 API")
@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @Operation(summary = "활동 로그 저장", description = "학생이 수행한 행위 결과를 JSON으로 저장")
    @PostMapping
    public ResponseEntity<ActivityLogResponse> createLog(@Valid @RequestBody ActivityLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityLogService.createLog(request));
    }

    @Operation(summary = "활동 이력 조회 (studentId 없으면 전체)")
    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> getLogs(@RequestParam(required = false) Integer studentId) {
        if (studentId != null) {
            return ResponseEntity.ok(activityLogService.getLogsByStudent(studentId));
        }
        return ResponseEntity.ok(activityLogService.getAllLogs());
    }

    @Operation(summary = "직업-행위별 활동 이력 조회")
    @GetMapping("/by-job-action")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByJobAction(@RequestParam Integer jobActionId) {
        return ResponseEntity.ok(activityLogService.getLogsByJobAction(jobActionId));
    }

    @Operation(summary = "워크플로우로 전달받은 문서 조회", description = "내 직업으로 향하는 워크플로우의 발신 직업들에서 제출된 로그")
    @GetMapping("/received")
    public ResponseEntity<List<ActivityLogResponse>> getReceivedLogs(@RequestParam Integer studentId) {
        return ResponseEntity.ok(activityLogService.getReceivedLogs(studentId));
    }
}
