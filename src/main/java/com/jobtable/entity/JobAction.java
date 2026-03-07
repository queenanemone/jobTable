package com.jobtable.entity;

import com.jobtable.converter.JsonConverter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(
    name = "Job_Actions",
    uniqueConstraints = @UniqueConstraint(name = "uq_job_action", columnNames = {"job_id", "action_id"})
)
public class JobAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobTemplate job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionMaster action;

    @Column(name = "action_config", columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> actionConfig;

    @OneToMany(mappedBy = "jobAction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityLog> activityLogs = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public JobTemplate getJob() { return job; }
    public void setJob(JobTemplate job) { this.job = job; }

    public ActionMaster getAction() { return action; }
    public void setAction(ActionMaster action) { this.action = action; }

    public Map<String, Object> getActionConfig() { return actionConfig; }
    public void setActionConfig(Map<String, Object> actionConfig) { this.actionConfig = actionConfig; }

    public List<ActivityLog> getActivityLogs() { return activityLogs; }
    public void setActivityLogs(List<ActivityLog> activityLogs) { this.activityLogs = activityLogs; }
}
