package com.polyconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sbtet_exam_type")
public class SbtetExamType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sbtet_exam_type_id", nullable = false, unique = true)
    private Integer sbtetExamTypeId; // 1 (Mid1), 2 (Mid2), 5 (Semester)

    @Column(name = "exam_name", nullable = false, length = 50)
    private String examName;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SbtetExamType() {}

    public SbtetExamType(Integer sbtetExamTypeId, String examName, Boolean active) {
        this.sbtetExamTypeId = sbtetExamTypeId;
        this.examName = examName;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSbtetExamTypeId() { return sbtetExamTypeId; }
    public void setSbtetExamTypeId(Integer sbtetExamTypeId) { this.sbtetExamTypeId = sbtetExamTypeId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
