package com.jobtable.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "action_master")
public class ActionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "action_code", nullable = false, unique = true, length = 20)
    private String actionCode;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL)
    private List<JobAction> jobActions = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<JobAction> getJobActions() { return jobActions; }
    public void setJobActions(List<JobAction> jobActions) { this.jobActions = jobActions; }
}
