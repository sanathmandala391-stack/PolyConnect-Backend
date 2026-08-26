package com.polyconnect.entity;



import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ExamMonthYearResponse {
    @JsonProperty("Table")
    private List<StatusTable> table;

    public List<StatusTable> getTable() {
        return table;
    }

    public void setTable(List<StatusTable> table) {
        this.table = table;
    }

    public List<ExamMonthYearData> getTable1() {
        return table1;
    }

    public void setTable1(List<ExamMonthYearData> table1) {
        this.table1 = table1;
    }

    @JsonProperty("Table1")
    private List<ExamMonthYearData> table1;

    public static class StatusTable {
        @JsonProperty("ResponceCode")
        private String responseCode;
        @JsonProperty("ResponceDescription")
        private String responseDescription;

        public String getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(String responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponseDescription() {
            return responseDescription;
        }

        public void setResponseDescription(String responseDescription) {
            this.responseDescription = responseDescription;
        }
    }

    public static class ExamMonthYearData {
        @JsonProperty("Id")
        private Long id;
        @JsonProperty("ExamYearMonth")
        private String examYearMonth;
        @JsonProperty("SequenceId")
        private Double sequenceId;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
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


}