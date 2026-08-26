package com.polyconnect.entity;

public class ExamMonthYearOption {
    private Integer id;
    private String examYearMonth;
    private Double sequenceId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExamYearMonth() {
        return examYearMonth;
    }

    public void setExamYearMonth(String examYearMonth) {
        this.examYearMonth = examYearMonth;
    }

    public Double getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Double sequenceId) {
        this.sequenceId = sequenceId;
    }
}
