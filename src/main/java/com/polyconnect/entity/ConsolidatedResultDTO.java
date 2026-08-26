package com.polyconnect.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ConsolidatedResultDTO {

    @JsonProperty("StudentDetails")
    private Object studentDetails;

    @JsonProperty("Semesters")
    private List<Object> semesters;

    @JsonProperty("OverallStatus")
    private String overallStatus;

    // Getters and Setters
    public Object getStudentDetails() {
        return studentDetails;
    }

    public void setStudentDetails(Object studentDetails) {
        this.studentDetails = studentDetails;
    }

    public List<Object> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<Object> semesters) {
        this.semesters = semesters;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }
}