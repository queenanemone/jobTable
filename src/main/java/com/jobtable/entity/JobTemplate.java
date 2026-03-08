package com.jobtable.entity;

import com.jobtable.converter.JsonConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "Job_Templates")
public class JobTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "base_salary")
    private Integer baseSalary = 0;

    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> attributes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobAction> jobActions = new ArrayList<>();

    @OneToMany(mappedBy = "fromJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobWorkflow> fromWorkflows = new ArrayList<>();

    @OneToMany(mappedBy = "toJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobWorkflow> toWorkflows = new ArrayList<>();

    @OneToMany(mappedBy = "currentJob")
    private List<Student> students = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getBaseSalary() { return baseSalary; }
    public void setBaseSalary(Integer baseSalary) { this.baseSalary = baseSalary; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<JobAction> getJobActions() { return jobActions; }
    public void setJobActions(List<JobAction> jobActions) { this.jobActions = jobActions; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }
}
