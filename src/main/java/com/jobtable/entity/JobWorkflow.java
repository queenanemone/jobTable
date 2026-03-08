package com.jobtable.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Job_Workflow")
public class JobWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_job_id", nullable = false)
    private JobTemplate fromJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_job_id", nullable = false)
    private JobTemplate toJob;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(length = 200)
    private String description;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public JobTemplate getFromJob() { return fromJob; }
    public void setFromJob(JobTemplate fromJob) { this.fromJob = fromJob; }

    public JobTemplate getToJob() { return toJob; }
    public void setToJob(JobTemplate toJob) { this.toJob = toJob; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
