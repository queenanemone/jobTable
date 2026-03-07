package com.jobtable.controller;

import com.jobtable.dto.action.ActionRequest;
import com.jobtable.dto.action.ActionResponse;
import com.jobtable.service.ActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Action", description = "행위 마스터 관리 API")
@RestController
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @Operation(summary = "행위 목록 조회")
    @GetMapping
    public ResponseEntity<List<ActionResponse>> getAllActions() {
        return ResponseEntity.ok(actionService.getAllActions());
    }

    @Operation(summary = "행위 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ActionResponse> getAction(@PathVariable Integer id) {
        return ResponseEntity.ok(actionService.getAction(id));
    }

    @Operation(summary = "행위 생성", description = "actionCode는 자동으로 대문자로 저장됩니다. 예: LEDGER, CHECKLIST")
    @PostMapping
    public ResponseEntity<ActionResponse> createAction(@Valid @RequestBody ActionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actionService.createAction(request));
    }

    @Operation(summary = "행위 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAction(@PathVariable Integer id) {
        actionService.deleteAction(id);
        return ResponseEntity.noContent().build();
    }
}
