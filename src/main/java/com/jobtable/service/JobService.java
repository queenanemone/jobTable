package com.jobtable.service;

import com.jobtable.dto.job.JobRequest;
import com.jobtable.dto.job.JobResponse;
import com.jobtable.dto.jobaction.JobActionRequest;
import com.jobtable.dto.jobaction.JobActionResponse;
import com.jobtable.entity.ActionMaster;
import com.jobtable.entity.JobAction;
import com.jobtable.entity.JobTemplate;
import com.jobtable.repository.ActionMasterRepository;
import com.jobtable.repository.JobActionRepository;
import com.jobtable.repository.JobTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class JobService {

    private final JobTemplateRepository jobRepository;
    private final ActionMasterRepository actionRepository;
    private final JobActionRepository jobActionRepository;

    public JobService(JobTemplateRepository jobRepository,
                      ActionMasterRepository actionRepository,
                      JobActionRepository jobActionRepository) {
        this.jobRepository = jobRepository;
        this.actionRepository = actionRepository;
        this.jobActionRepository = jobActionRepository;
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(JobResponse::from)
                .toList();
    }

    public JobResponse getJob(Integer id) {
        JobTemplate job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("직업을 찾을 수 없습니다. id=" + id));
        return JobResponse.from(job);
    }

    @Transactional
    public JobResponse createJob(JobRequest request) {
        JobTemplate job = new JobTemplate();
        applyRequest(job, request);
        return JobResponse.from(jobRepository.save(job));
    }

    @Transactional
    public JobResponse updateJob(Integer id, JobRequest request) {
        JobTemplate job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("직업을 찾을 수 없습니다. id=" + id));
        applyRequest(job, request);
        return JobResponse.from(job);
    }

    private void applyRequest(JobTemplate job, JobRequest request) {
        job.setName(request.getName());
        job.setBaseSalary(request.getBaseSalary() != null ? request.getBaseSalary() : 0);
        job.setColor(request.getColor());
        job.setIcon(request.getIcon());
        job.setDescription(request.getDescription());
        job.setMaxCount(request.getMaxCount());
        job.setIsRequired(request.getIsRequired() != null ? request.getIsRequired() : false);
        job.setAttributes(request.getAttributes());
    }

    @Transactional
    public void deleteJob(Integer id) {
        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("직업을 찾을 수 없습니다. id=" + id);
        }
        jobRepository.deleteById(id);
    }

    public JobActionResponse getJobAction(Integer jobActionId) {
        JobAction ja = jobActionRepository.findById(jobActionId)
                .orElseThrow(() -> new RuntimeException("직업-행위를 찾을 수 없습니다. id=" + jobActionId));
        return JobActionResponse.from(ja);
    }

    // 명세서 6항: /jobs/{id}/actions - 해당 직업의 모든 행위 설정(Schema) 반환
    public List<JobActionResponse> getJobActions(Integer jobId) {
        return jobActionRepository.findByJobId(jobId).stream()
                .map(JobActionResponse::from)
                .toList();
    }

    @Transactional
    public void deleteJobAction(Integer jobActionId) {
        if (!jobActionRepository.existsById(jobActionId)) {
            throw new RuntimeException("직업-행위를 찾을 수 없습니다. id=" + jobActionId);
        }
        jobActionRepository.deleteById(jobActionId);
    }

    @Transactional
    public JobActionResponse addJobAction(Integer jobId, JobActionRequest request) {
        JobTemplate job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("직업을 찾을 수 없습니다. id=" + jobId));
        ActionMaster action = actionRepository.findById(request.getActionId())
                .orElseThrow(() -> new RuntimeException("행위를 찾을 수 없습니다. id=" + request.getActionId()));

        JobAction jobAction = new JobAction();
        jobAction.setJob(job);
        jobAction.setAction(action);
        return JobActionResponse.from(jobActionRepository.save(jobAction));
    }
}
