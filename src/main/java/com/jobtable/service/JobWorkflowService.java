package com.jobtable.service;

import com.jobtable.dto.workflow.JobWorkflowRequest;
import com.jobtable.dto.workflow.JobWorkflowResponse;
import com.jobtable.entity.JobTemplate;
import com.jobtable.entity.JobWorkflow;
import com.jobtable.repository.JobTemplateRepository;
import com.jobtable.repository.JobWorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class JobWorkflowService {

    private final JobWorkflowRepository workflowRepository;
    private final JobTemplateRepository jobRepository;

    public JobWorkflowService(JobWorkflowRepository workflowRepository,
                              JobTemplateRepository jobRepository) {
        this.workflowRepository = workflowRepository;
        this.jobRepository = jobRepository;
    }

    public List<JobWorkflowResponse> getAllWorkflows() {
        return workflowRepository.findAll().stream()
                .map(JobWorkflowResponse::from)
                .toList();
    }

    public List<JobWorkflowResponse> getWorkflowsByFromJob(Integer fromJobId) {
        return workflowRepository.findByFromJobId(fromJobId).stream()
                .map(JobWorkflowResponse::from)
                .toList();
    }

    public List<JobWorkflowResponse> getWorkflowsByToJob(Integer toJobId) {
        return workflowRepository.findByToJobId(toJobId).stream()
                .map(JobWorkflowResponse::from)
                .toList();
    }

    @Transactional
    public JobWorkflowResponse createWorkflow(JobWorkflowRequest request) {
        JobTemplate fromJob = jobRepository.findById(request.getFromJobId())
                .orElseThrow(() -> new RuntimeException("직업을 찾을 수 없습니다. id=" + request.getFromJobId()));
        JobTemplate toJob = jobRepository.findById(request.getToJobId())
                .orElseThrow(() -> new RuntimeException("직업을 찾을 수 없습니다. id=" + request.getToJobId()));

        JobWorkflow workflow = new JobWorkflow();
        workflow.setFromJob(fromJob);
        workflow.setToJob(toJob);
        workflow.setDocumentType(request.getDocumentType());
        workflow.setDescription(request.getDescription());
        return JobWorkflowResponse.from(workflowRepository.save(workflow));
    }

    @Transactional
    public void deleteWorkflow(Integer id) {
        if (!workflowRepository.existsById(id)) {
            throw new RuntimeException("워크플로우를 찾을 수 없습니다. id=" + id);
        }
        workflowRepository.deleteById(id);
    }
}
