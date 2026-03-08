package com.jobtable.entity;

import com.jobtable.converter.JsonConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "Action_Master")
public class ActionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "action_code", nullable = false, unique = true, length = 20)
    private String actionCode;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "action_config", columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> actionConfig;

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

    public Map<String, Object> getActionConfig() { return actionConfig; }
    public void setActionConfig(Map<String, Object> actionConfig) { this.actionConfig = actionConfig; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<JobAction> getJobActions() { return jobActions; }
    public void setJobActions(List<JobAction> jobActions) { this.jobActions = jobActions; }
}
