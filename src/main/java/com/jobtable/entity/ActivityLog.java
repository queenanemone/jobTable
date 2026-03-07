package com.jobtable.entity;

import com.jobtable.converter.JsonConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "Activity_Logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_action_id", nullable = false)
    private JobAction jobAction;

    @Column(columnDefinition = "JSON", nullable = false)
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public JobAction getJobAction() { return jobAction; }
    public void setJobAction(JobAction jobAction) { this.jobAction = jobAction; }

    public Map<String, Object> getContent() { return content; }
    public void setContent(Map<String, Object> content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
