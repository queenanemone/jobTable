package com.jobtable.dto.action;

import com.jobtable.entity.ActionMaster;

import java.time.LocalDateTime;

public class ActionResponse {

    private Integer id;
    private String actionCode;
    private String displayName;
    private LocalDateTime createdAt;

    public static ActionResponse from(ActionMaster action) {
        ActionResponse r = new ActionResponse();
        r.id = action.getId();
        r.actionCode = action.getActionCode();
        r.displayName = action.getDisplayName();
        r.createdAt = action.getCreatedAt();
        return r;
    }

    public Integer getId() { return id; }
    public String getActionCode() { return actionCode; }
    public String getDisplayName() { return displayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
