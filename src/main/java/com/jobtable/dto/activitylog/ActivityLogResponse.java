package com.jobtable.dto.activitylog;

import com.jobtable.entity.ActivityLog;

import java.time.LocalDateTime;
import java.util.Map;

public class ActivityLogResponse {

    private Integer id;
    private Integer studentId;
    private String studentName;
    private Integer jobActionId;
    private String actionCode;
    private String jobName;
    private Map<String, Object> content;
    private LocalDateTime createdAt;

    public static ActivityLogResponse from(ActivityLog log) {
        ActivityLogResponse r = new ActivityLogResponse();
        r.id = log.getId();
        r.studentId = log.getStudent().getId();
        r.studentName = log.getStudent().getName();
        r.jobActionId = log.getJobAction().getId();
        r.actionCode = log.getJobAction().getAction().getActionCode();
        r.jobName = log.getJobAction().getJob().getName();
        r.content = log.getContent();
        r.createdAt = log.getCreatedAt();
        return r;
    }

    public Integer getId() { return id; }
    public Integer getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public Integer getJobActionId() { return jobActionId; }
    public String getActionCode() { return actionCode; }
    public String getJobName() { return jobName; }
    public Map<String, Object> getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
