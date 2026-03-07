package com.jobtable.repository;

import com.jobtable.entity.JobAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobActionRepository extends JpaRepository<JobAction, Integer> {

    // 명세서 6항: /jobs/{id}/actions API 지원을 위한 쿼리
    List<JobAction> findByJobId(Integer jobId);
}
