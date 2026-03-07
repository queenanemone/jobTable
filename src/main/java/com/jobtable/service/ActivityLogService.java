package com.jobtable.service;

import com.jobtable.dto.activitylog.ActivityLogRequest;
import com.jobtable.dto.activitylog.ActivityLogResponse;
import com.jobtable.entity.ActivityLog;
import com.jobtable.entity.JobAction;
import com.jobtable.entity.Student;
import com.jobtable.repository.ActivityLogRepository;
import com.jobtable.repository.JobActionRepository;
import com.jobtable.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ActivityLogService {

    private final ActivityLogRepository logRepository;
    private final StudentRepository studentRepository;
    private final JobActionRepository jobActionRepository;

    public ActivityLogService(ActivityLogRepository logRepository,
                              StudentRepository studentRepository,
                              JobActionRepository jobActionRepository) {
        this.logRepository = logRepository;
        this.studentRepository = studentRepository;
        this.jobActionRepository = jobActionRepository;
    }

    @Transactional
    public ActivityLogResponse createLog(ActivityLogRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다. id=" + request.getStudentId()));
        JobAction jobAction = jobActionRepository.findById(request.getJobActionId())
                .orElseThrow(() -> new RuntimeException("직업-행위를 찾을 수 없습니다. id=" + request.getJobActionId()));

        ActivityLog log = new ActivityLog();
        log.setStudent(student);
        log.setJobAction(jobAction);
        log.setContent(request.getContent());
        return ActivityLogResponse.from(logRepository.save(log));
    }

    public List<ActivityLogResponse> getLogsByStudent(Integer studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new RuntimeException("학생을 찾을 수 없습니다. id=" + studentId);
        }
        return logRepository.findByStudentId(studentId).stream()
                .map(ActivityLogResponse::from)
                .toList();
    }

    public List<ActivityLogResponse> getLogsByJobAction(Integer jobActionId) {
        return logRepository.findByJobActionId(jobActionId).stream()
                .map(ActivityLogResponse::from)
                .toList();
    }
}
