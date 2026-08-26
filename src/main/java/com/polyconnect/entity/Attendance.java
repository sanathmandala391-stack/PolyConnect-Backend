package com.polyconnect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_pin", nullable = false, unique = true, length = 20)
    private String studentPin;

    @Column(name = "college_id")
    private Long collegeId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "working_days", nullable = false)
    private Integer workingDays = 0;

    @Column(name = "present_days", nullable = false)
    private Integer presentDays = 0;

    @Column(name = "absent_days", nullable = false)
    private Integer absentDays = 0;

    @Column(name = "exams_working_days", nullable = false)
    private Integer examsWorkingDays = 0;

    // Current standing percentage: (present_days / working_days) * 100
    @Column(name = "current_standing_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal currentStandingPercentage = BigDecimal.ZERO;

    // Exam eligibility percentage: (present_days / exams_working_days) * 100
    @Column(name = "exam_eligibility_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal examEligibilityPercentage = BigDecimal.ZERO;

    @Column(name = "consecutive_absent_days", nullable = false)
    private Integer consecutiveAbsentDays = 0;

    // Detention risk is strictly evaluated based on exam eligibility percentage < 75.00%
    @Column(name = "is_detention_risk", nullable = false)
    private Boolean detentionRisk = false;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt = LocalDateTime.now();

    @Column(name = "raw_summary_json", columnDefinition = "JSON")
    private String rawSummaryJson;

    @Column(name = "raw_calendar_json", columnDefinition = "JSON")
    private String rawCalendarJson;

    @Column(name = "raw_month_lookup_json", columnDefinition = "JSON")
    private String rawMonthLookupJson;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Lob
    @Column(name = "daily_records_json", columnDefinition = "TEXT")
    private String dailyRecordsJson;

    public String getDailyRecordsJson() { return dailyRecordsJson; }
    public void setDailyRecordsJson(String dailyRecordsJson) { this.dailyRecordsJson = dailyRecordsJson; }

    public Attendance() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentPin() { return studentPin; }
    public void setStudentPin(String studentPin) { this.studentPin = studentPin; }

    public Long getCollegeId() { return collegeId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public Integer getWorkingDays() { return workingDays; }
    public void setWorkingDays(Integer workingDays) { this.workingDays = workingDays; }

    public Integer getPresentDays() { return presentDays; }
    public void setPresentDays(Integer presentDays) { this.presentDays = presentDays; }

    public Integer getAbsentDays() { return absentDays; }
    public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }

    public Integer getExamsWorkingDays() { return examsWorkingDays; }
    public void setExamsWorkingDays(Integer examsWorkingDays) { this.examsWorkingDays = examsWorkingDays; }

    public BigDecimal getCurrentStandingPercentage() { return currentStandingPercentage; }
    public void setCurrentStandingPercentage(BigDecimal currentStandingPercentage) { this.currentStandingPercentage = currentStandingPercentage; }

    public BigDecimal getExamEligibilityPercentage() { return examEligibilityPercentage; }
    public void setExamEligibilityPercentage(BigDecimal examEligibilityPercentage) {
        this.examEligibilityPercentage = examEligibilityPercentage;
        this.detentionRisk = examEligibilityPercentage.compareTo(new BigDecimal("75.00")) < 0;
    }

    public Integer getConsecutiveAbsentDays() { return consecutiveAbsentDays; }
    public void setConsecutiveAbsentDays(Integer consecutiveAbsentDays) { this.consecutiveAbsentDays = consecutiveAbsentDays; }

    public Boolean getDetentionRisk() { return detentionRisk; }
    public void setDetentionRisk(Boolean detentionRisk) { this.detentionRisk = detentionRisk; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public String getRawSummaryJson() { return rawSummaryJson; }
    public void setRawSummaryJson(String rawSummaryJson) { this.rawSummaryJson = rawSummaryJson; }

    public String getRawCalendarJson() { return rawCalendarJson; }
    public void setRawCalendarJson(String rawCalendarJson) { this.rawCalendarJson = rawCalendarJson; }

    public String getRawMonthLookupJson() { return rawMonthLookupJson; }
    public void setRawMonthLookupJson(String rawMonthLookupJson) { this.rawMonthLookupJson = rawMonthLookupJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
