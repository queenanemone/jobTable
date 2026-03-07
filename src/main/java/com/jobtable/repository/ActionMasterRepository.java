package com.jobtable.repository;

import com.jobtable.entity.ActionMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActionMasterRepository extends JpaRepository<ActionMaster, Integer> {

    Optional<ActionMaster> findByActionCode(String actionCode);
}
