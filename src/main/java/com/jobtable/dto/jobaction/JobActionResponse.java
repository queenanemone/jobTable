package com.jobtable.dto.jobaction;

import com.jobtable.entity.JobAction;
import java.util.Map;

public class JobActionResponse {

    private Integer id;
    private Integer jobId;
    private Integer actionId;
    private String actionCode;
    private String displayName;
    private Map<String, Object> actionConfig;

    public static JobActionResponse from(JobAction ja) {
        JobActionResponse r = new JobActionResponse();
        r.id = ja.getId();
        r.jobId = ja.getJob().getId();
        r.actionId = ja.getAction().getId();
        r.actionCode = ja.getAction().getActionCode();
        r.displayName = ja.getAction().getDisplayName();
        r.actionConfig = ja.getActionConfig();
        return r;
    }

    public Integer getId() { return id; }
    public Integer getJobId() { return jobId; }
    public Integer getActionId() { return actionId; }
    public String getActionCode() { return actionCode; }
    public String getDisplayName() { return displayName; }
    public Map<String, Object> getActionConfig() { return actionConfig; }
}
