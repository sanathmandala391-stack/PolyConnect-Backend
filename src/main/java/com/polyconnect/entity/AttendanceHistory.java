package com.polyconnect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_history")
public class AttendanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_pin", nullable = false, length = 20)
    private String studentPin;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "current_standing_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal currentStandingPercentage;

    @Column(name = "exam_eligibility_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal examEligibilityPercentage;

    @Column(name = "present_days", nullable = false)
    private Integer presentDays;

    @Column(name = "working_days", nullable = false)
    private Integer workingDays;

    @Column(name = "exams_working_days", nullable = false)
    private Integer examsWorkingDays;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AttendanceHistory() {}

    public AttendanceHistory(String studentPin, LocalDate snapshotDate, BigDecimal currentStandingPercentage,
                             BigDecimal examEligibilityPercentage, Integer presentDays, Integer workingDays,
                             Integer examsWorkingDays) {
        this.studentPin = studentPin;
        this.snapshotDate = snapshotDate;
        this.currentStandingPercentage = currentStandingPercentage;
        this.examEligibilityPercentage = examEligibilityPercentage;
        this.presentDays = presentDays;
        this.workingDays = workingDays;
        this.examsWorkingDays = examsWorkingDays;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentPin() { return studentPin; }
    public void setStudentPin(String studentPin) { this.studentPin = studentPin; }

    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }

    public BigDecimal getCurrentStandingPercentage() { return currentStandingPercentage; }
    public void setCurrentStandingPercentage(BigDecimal currentStandingPercentage) { this.currentStandingPercentage = currentStandingPercentage; }

    public BigDecimal getExamEligibilityPercentage() { return examEligibilityPercentage; }
    public void setExamEligibilityPercentage(BigDecimal examEligibilityPercentage) { this.examEligibilityPercentage = examEligibilityPercentage; }

    public Integer getPresentDays() { return presentDays; }
    public void setPresentDays(Integer presentDays) { this.presentDays = presentDays; }

    public Integer getWorkingDays() { return workingDays; }
    public void setWorkingDays(Integer workingDays) { this.workingDays = workingDays; }

    public Integer getExamsWorkingDays() { return examsWorkingDays; }
    public void setExamsWorkingDays(Integer examsWorkingDays) { this.examsWorkingDays = examsWorkingDays; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
