package com.jobtable.dto.action;

import com.jobtable.entity.ActionMaster;

import java.time.LocalDateTime;
import java.util.Map;

public class ActionResponse {

    private Integer id;
    private String actionCode;
    private String displayName;
    private Map<String, Object> actionConfig;
    private LocalDateTime createdAt;

    public static ActionResponse from(ActionMaster action) {
        ActionResponse r = new ActionResponse();
        r.id = action.getId();
        r.actionCode = action.getActionCode();
        r.displayName = action.getDisplayName();
        r.actionConfig = action.getActionConfig();
        r.createdAt = action.getCreatedAt();
        return r;
    }

    public Integer getId() { return id; }
    public String getActionCode() { return actionCode; }
    public String getDisplayName() { return displayName; }
    public Map<String, Object> getActionConfig() { return actionConfig; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
