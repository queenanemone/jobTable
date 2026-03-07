package com.jobtable.dto.job;

import com.jobtable.entity.JobTemplate;

import java.time.LocalDateTime;
import java.util.Map;

public class JobResponse {

    private Integer id;
    private String name;
    private Integer baseSalary;
    private Map<String, Object> attributes;
    private LocalDateTime createdAt;

    public static JobResponse from(JobTemplate job) {
        JobResponse r = new JobResponse();
        r.id = job.getId();
        r.name = job.getName();
        r.baseSalary = job.getBaseSalary();
        r.attributes = job.getAttributes();
        r.createdAt = job.getCreatedAt();
        return r;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getBaseSalary() { return baseSalary; }
    public Map<String, Object> getAttributes() { return attributes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
