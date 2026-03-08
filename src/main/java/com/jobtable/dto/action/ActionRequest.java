package com.jobtable.dto.action;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public class ActionRequest {

    @NotBlank
    @Size(max = 20)
    private String actionCode;

    @NotBlank
    @Size(max = 50)
    private String displayName;

    private Map<String, Object> actionConfig;

    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Map<String, Object> getActionConfig() { return actionConfig; }
    public void setActionConfig(Map<String, Object> actionConfig) { this.actionConfig = actionConfig; }
}
