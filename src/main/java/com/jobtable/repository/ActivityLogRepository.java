package com.jobtable.repository;

import com.jobtable.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    List<ActivityLog> findByStudentId(Integer studentId);

    List<ActivityLog> findByJobActionId(Integer jobActionId);
}
