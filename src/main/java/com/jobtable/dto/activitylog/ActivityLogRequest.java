package com.jobtable.dto.activitylog;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class ActivityLogRequest {

    @NotNull
    private Integer studentId;

    @NotNull
    private Integer jobActionId;

    @NotNull
    private Map<String, Object> content;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getJobActionId() { return jobActionId; }
    public void setJobActionId(Integer jobActionId) { this.jobActionId = jobActionId; }

    public Map<String, Object> getContent() { return content; }
    public void setContent(Map<String, Object> content) { this.content = content; }
}
