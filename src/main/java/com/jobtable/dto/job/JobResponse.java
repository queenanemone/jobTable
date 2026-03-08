package com.jobtable.dto.job;

import com.jobtable.entity.JobTemplate;

import java.time.LocalDateTime;
import java.util.Map;

public class JobResponse {

    private Integer id;
    private String name;
    private Integer baseSalary;
    private String color;
    private String icon;
    private String description;
    private Integer maxCount;
    private Boolean isRequired;
    private Map<String, Object> attributes;
    private LocalDateTime createdAt;

    public static JobResponse from(JobTemplate job) {
        JobResponse r = new JobResponse();
        r.id = job.getId();
        r.name = job.getName();
        r.baseSalary = job.getBaseSalary();
        r.color = job.getColor();
        r.icon = job.getIcon();
        r.description = job.getDescription();
        r.maxCount = job.getMaxCount();
        r.isRequired = job.getIsRequired();
        r.attributes = job.getAttributes();
        r.createdAt = job.getCreatedAt();
        return r;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getBaseSalary() { return baseSalary; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public Integer getMaxCount() { return maxCount; }
    public Boolean getIsRequired() { return isRequired; }
    public Map<String, Object> getAttributes() { return attributes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
