package com.jobtable.service;

import com.jobtable.dto.action.ActionRequest;
import com.jobtable.dto.action.ActionResponse;
import com.jobtable.entity.ActionMaster;
import com.jobtable.repository.ActionMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ActionService {

    private final ActionMasterRepository actionRepository;

    public ActionService(ActionMasterRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    public List<ActionResponse> getAllActions() {
        return actionRepository.findAll().stream()
                .map(ActionResponse::from)
                .toList();
    }

    public ActionResponse getAction(Integer id) {
        ActionMaster action = actionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("행위를 찾을 수 없습니다. id=" + id));
        return ActionResponse.from(action);
    }

    @Transactional
    public ActionResponse createAction(ActionRequest request) {
        ActionMaster action = new ActionMaster();
        action.setActionCode(request.getActionCode().toUpperCase());
        action.setDisplayName(request.getDisplayName());
        return ActionResponse.from(actionRepository.save(action));
    }

    @Transactional
    public void deleteAction(Integer id) {
        if (!actionRepository.existsById(id)) {
            throw new RuntimeException("행위를 찾을 수 없습니다. id=" + id);
        }
        actionRepository.deleteById(id);
    }
}
