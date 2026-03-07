package com.jobtable.dto.jobaction;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class JobActionRequest {

    @NotNull
    private Integer actionId;

    private Map<String, Object> actionConfig;

    public Integer getActionId() { return actionId; }
    public void setActionId(Integer actionId) { this.actionId = actionId; }

    public Map<String, Object> getActionConfig() { return actionConfig; }
    public void setActionConfig(Map<String, Object> actionConfig) { this.actionConfig = actionConfig; }
}
