package com.jobtable.dto.student;

import com.jobtable.entity.Student;

public class StudentResponse {

    private Integer id;
    private String name;
    private Integer currentJobId;
    private String currentJobName;
    private Integer balance;

    public static StudentResponse from(Student student) {
        StudentResponse r = new StudentResponse();
        r.id = student.getId();
        r.name = student.getName();
        r.balance = student.getBalance();
        if (student.getCurrentJob() != null) {
            r.currentJobId = student.getCurrentJob().getId();
            r.currentJobName = student.getCurrentJob().getName();
        }
        return r;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getCurrentJobId() { return currentJobId; }
    public String getCurrentJobName() { return currentJobName; }
    public Integer getBalance() { return balance; }
}
