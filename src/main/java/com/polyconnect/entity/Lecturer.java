package com.polyconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lecturers")
@PrimaryKeyJoinColumn(name = "id")
public class Lecturer extends User {

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(length = 100)
    private String designation;

    @Column(length = 150)
    private String specialization;

    public Lecturer() {
        setRole(Role.LECTURER);
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
