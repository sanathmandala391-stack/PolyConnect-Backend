package com.polyconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sbtet_scheme")
public class SbtetScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheme_code", nullable = false, unique = true, length = 20)
    private String schemeCode; // C26, C24, ER2020, C21, etc.

    @Column(name = "sbtet_scheme_id", nullable = false)
    private Integer sbtetSchemeId; // 12, 11, 10, 9, etc.

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SbtetScheme() {}

    public SbtetScheme(String schemeCode, Integer sbtetSchemeId, Boolean active) {
        this.schemeCode = schemeCode;
        this.sbtetSchemeId = sbtetSchemeId;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSchemeCode() { return schemeCode; }
    public void setSchemeCode(String schemeCode) { this.schemeCode = schemeCode; }

    public Integer getSbtetSchemeId() { return sbtetSchemeId; }
    public void setSbtetSchemeId(Integer sbtetSchemeId) { this.sbtetSchemeId = sbtetSchemeId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
