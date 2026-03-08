package com.jobtable.dto.job;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class JobRequest {

    @NotBlank
    private String name;

    private Integer baseSalary = 0;

    private String color;
    private String icon;
    private String description;
    private Integer maxCount;
    private Boolean isRequired = false;

    private Map<String, Object> attributes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getBaseSalary() { return baseSalary; }
    public void setBaseSalary(Integer baseSalary) { this.baseSalary = baseSalary; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxCount() { return maxCount; }
    public void setMaxCount(Integer maxCount) { this.maxCount = maxCount; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}
