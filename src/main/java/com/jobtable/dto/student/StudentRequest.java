package com.jobtable.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StudentRequest {

    @NotBlank
    @Size(max = 20)
    private String name;

    private Integer currentJobId;

    private Integer balance = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCurrentJobId() { return currentJobId; }
    public void setCurrentJobId(Integer currentJobId) { this.currentJobId = currentJobId; }

    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }
}
