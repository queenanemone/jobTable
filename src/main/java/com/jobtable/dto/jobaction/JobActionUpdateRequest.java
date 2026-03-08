package com.jobtable.dto.jobaction;

import java.util.Map;

public class JobActionUpdateRequest {

    private Map<String, Object> actionConfig;

    public Map<String, Object> getActionConfig() { return actionConfig; }
    public void setActionConfig(Map<String, Object> actionConfig) { this.actionConfig = actionConfig; }
}
