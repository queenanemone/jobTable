package com.jobtable.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_job_id")
    private JobTemplate currentJob;

    @Column
    private Integer balance = 0;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityLog> activityLogs = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public JobTemplate getCurrentJob() { return currentJob; }
    public void setCurrentJob(JobTemplate currentJob) { this.currentJob = currentJob; }

    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }

    public List<ActivityLog> getActivityLogs() { return activityLogs; }
    public void setActivityLogs(List<ActivityLog> activityLogs) { this.activityLogs = activityLogs; }
}
