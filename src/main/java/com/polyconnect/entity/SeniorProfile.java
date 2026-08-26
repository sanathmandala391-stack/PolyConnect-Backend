package com.polyconnect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "senior_profiles")
public class SeniorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_company_or_college")
    private String currentCompanyOrCollege;

    @Column(length = 150)
    private String designation;

    @Column(name = "ecet_rank")
    private Integer ecetRank;

    @Column(name = "diploma_score_percentage", precision = 5, scale = 2)
    private BigDecimal diplomaScorePercentage;

    @Column(length = 500)
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "is_available_for_mentorship")
    private Boolean availableForMentorship = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SeniorProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCurrentCompanyOrCollege() { return currentCompanyOrCollege; }
    public void setCurrentCompanyOrCollege(String currentCompanyOrCollege) { this.currentCompanyOrCollege = currentCompanyOrCollege; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getEcetRank() { return ecetRank; }
    public void setEcetRank(Integer ecetRank) { this.ecetRank = ecetRank; }

    public BigDecimal getDiplomaScorePercentage() { return diplomaScorePercentage; }
    public void setDiplomaScorePercentage(BigDecimal diplomaScorePercentage) { this.diplomaScorePercentage = diplomaScorePercentage; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public Boolean getAvailableForMentorship() { return availableForMentorship; }
    public void setAvailableForMentorship(Boolean availableForMentorship) { this.availableForMentorship = availableForMentorship; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
