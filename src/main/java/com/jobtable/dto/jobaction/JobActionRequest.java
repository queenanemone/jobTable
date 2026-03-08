package com.jobtable.dto.jobaction;

import jakarta.validation.constraints.NotNull;

public class JobActionRequest {

    @NotNull
    private Integer actionId;

    public Integer getActionId() { return actionId; }
    public void setActionId(Integer actionId) { this.actionId = actionId; }
}
