package com.polyconnect.entity;

import java.util.List;

// HallticketResponse.java — mirrors SBTET's Table1 + Table2 shape
public class HallticketResponse {
    private List<StudentInfo> table1;
    private List<ExamSubject> table2;

    public static class StudentInfo {
        private String photo;          // base64 data URI, ready to use in <img src>
        private String pin;
        private String name;
        private String fatherName;
        private String scheme;
        private String collegeCode;
        private String branch;
        private String examinationCenter;
        private Double totalFeePaid;
        private Double attendance;
        private String examMonthYear;
        private String referenceNumber;
        // getters/setters
    }

    public static class ExamSubject {
        private String semester;
        private String subjectCode;
        private String subjectName;
        private String examDate;
        private String examTime;
        private Integer subjectOrder;
        // getters/setters
    }
    // getters/setters for table1, table2
}