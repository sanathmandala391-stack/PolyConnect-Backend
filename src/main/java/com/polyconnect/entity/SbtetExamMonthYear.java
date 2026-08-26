package com.polyconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sbtet_exam_month_year")
public class SbtetExamMonthYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sbtet_id", nullable = false, unique = true)
    private Integer sbtetId; // e.g. 103, 99, 87, 85, etc.

    @Column(name = "exam_year_month", nullable = false, length = 100)
    private String examYearMonth; // e.g. "OCT-2026", "APR-2026"

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SbtetExamMonthYear() {}

    public SbtetExamMonthYear(Integer sbtetId, String examYearMonth, Boolean active) {
        this.sbtetId = sbtetId;
        this.examYearMonth = examYearMonth;
        this.active = active;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSbtetId() { return sbtetId; }
    public void setSbtetId(Integer sbtetId) { this.sbtetId = sbtetId; }

    public String getExamYearMonth() { return examYearMonth; }
    public void setExamYearMonth(String examYearMonth) { this.examYearMonth = examYearMonth; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
