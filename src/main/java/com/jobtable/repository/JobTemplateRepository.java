package com.jobtable.repository;

import com.jobtable.entity.JobTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobTemplateRepository extends JpaRepository<JobTemplate, Integer> {
}
