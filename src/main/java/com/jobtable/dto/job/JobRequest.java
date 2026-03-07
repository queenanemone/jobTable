package com.jobtable.dto.job;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class JobRequest {

    @NotBlank
    private String name;

    private Integer baseSalary = 0;

    private Map<String, Object> attributes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getBaseSalary() { return baseSalary; }
    public void setBaseSalary(Integer baseSalary) { this.baseSalary = baseSalary; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}
