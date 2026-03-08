package com.jobtable.dto.workflow;

import jakarta.validation.constraints.NotNull;

public class JobWorkflowRequest {

    @NotNull
    private Integer fromJobId;

    @NotNull
    private Integer toJobId;

    private String documentType;

    private String description;

    public Integer getFromJobId() { return fromJobId; }
    public void setFromJobId(Integer fromJobId) { this.fromJobId = fromJobId; }

    public Integer getToJobId() { return toJobId; }
    public void setToJobId(Integer toJobId) { this.toJobId = toJobId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
