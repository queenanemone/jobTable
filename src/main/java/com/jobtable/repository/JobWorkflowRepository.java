package com.jobtable.repository;

import com.jobtable.entity.JobWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobWorkflowRepository extends JpaRepository<JobWorkflow, Integer> {

    List<JobWorkflow> findByFromJobId(Integer fromJobId);

    List<JobWorkflow> findByToJobId(Integer toJobId);
}
