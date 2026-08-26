package com.polyconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hods")
@PrimaryKeyJoinColumn(name = "id")
public class Hod extends User {

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(length = 100)
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears = 0;

    public Hod() {
        setRole(Role.HOD);
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
}
