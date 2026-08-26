package com.polyconnect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "id")
public class Student extends User {

    @Column(nullable = false, unique = true, length = 20)
    private String pin; // e.g. "24047-CS-023"

    @Column(name = "scheme_code", nullable = false, length = 20)
    private String schemeCode; // e.g. "C24", "C21"

    @Column(name = "current_semester", nullable = false, length = 10)
    private String currentSemester; // "1SEM", "2SEM", etc.

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(precision = 3, scale = 2)
    private BigDecimal cgpa = BigDecimal.ZERO;

    @Column(name = "total_backlogs")
    private Integer totalBacklogs = 0;

    @Column(name = "exam_eligibility_percentage", precision = 5, scale = 2)
    private BigDecimal examEligibilityPercentage = BigDecimal.ZERO;

    @Column(name = "current_standing_percentage", precision = 5, scale = 2)
    private BigDecimal currentStandingPercentage = BigDecimal.ZERO;

    @Column(name = "is_detention_risk")
    private Boolean detentionRisk = false;

    public Student() {
        setRole(Role.STUDENT);
    }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getSchemeCode() { return schemeCode; }
    public void setSchemeCode(String schemeCode) { this.schemeCode = schemeCode; }

    public String getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(String currentSemester) { this.currentSemester = currentSemester; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }

    public Integer getTotalBacklogs() { return totalBacklogs; }
    public void setTotalBacklogs(Integer totalBacklogs) { this.totalBacklogs = totalBacklogs; }

    public BigDecimal getExamEligibilityPercentage() { return examEligibilityPercentage; }
    public void setExamEligibilityPercentage(BigDecimal examEligibilityPercentage) { this.examEligibilityPercentage = examEligibilityPercentage; }

    public BigDecimal getCurrentStandingPercentage() { return currentStandingPercentage; }
    public void setCurrentStandingPercentage(BigDecimal currentStandingPercentage) { this.currentStandingPercentage = currentStandingPercentage; }

    public Boolean getDetentionRisk() { return detentionRisk; }
    public void setDetentionRisk(Boolean detentionRisk) { this.detentionRisk = detentionRisk; }
}
