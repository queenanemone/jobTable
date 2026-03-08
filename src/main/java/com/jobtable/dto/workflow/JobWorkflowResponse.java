package com.jobtable.dto.workflow;

import com.jobtable.entity.JobWorkflow;

public class JobWorkflowResponse {

    private Integer id;
    private Integer fromJobId;
    private String fromJobName;
    private Integer toJobId;
    private String toJobName;
    private String documentType;
    private String description;

    public static JobWorkflowResponse from(JobWorkflow w) {
        JobWorkflowResponse r = new JobWorkflowResponse();
        r.id = w.getId();
        r.fromJobId = w.getFromJob().getId();
        r.fromJobName = w.getFromJob().getName();
        r.toJobId = w.getToJob().getId();
        r.toJobName = w.getToJob().getName();
        r.documentType = w.getDocumentType();
        r.description = w.getDescription();
        return r;
    }

    public Integer getId() { return id; }
    public Integer getFromJobId() { return fromJobId; }
    public String getFromJobName() { return fromJobName; }
    public Integer getToJobId() { return toJobId; }
    public String getToJobName() { return toJobName; }
    public String getDocumentType() { return documentType; }
    public String getDescription() { return description; }
}
