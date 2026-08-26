package com.polyconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sbtet_semester")
public class SbtetSemester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sem_id", nullable = false, unique = true, length = 20)
    private String semId; // "1SEM", "2SEM", etc.

    @Column(name = "sequence_id", nullable = false)
    private Integer sequenceId; // 4, 5, 6, 7, 8, 9

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SbtetSemester() {}

    public SbtetSemester(String semId, Integer sequenceId, Boolean active) {
        this.semId = semId;
        this.sequenceId = sequenceId;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSemId() { return semId; }
    public void setSemId(String semId) { this.semId = semId; }

    public Integer getSequenceId() { return sequenceId; }
    public void setSequenceId(Integer sequenceId) { this.sequenceId = sequenceId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
