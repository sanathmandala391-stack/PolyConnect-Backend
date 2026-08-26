//package com.polyconnect.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "hod_approvals")
//public class HodApproval {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "hod_id", nullable = false)
//    private User hod;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "college_id", nullable = false)
//    private College college;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "branch_id", nullable = false)
//    private Branch branch;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 30)
//    private UserStatus status = UserStatus.PENDING;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "decided_by")
//    private User decidedBy;
//
//    @Column(name = "decision_reason")
//    private String decisionReason;
//
//    @Column(name = "decided_at")
//    private LocalDateTime decidedAt;
//
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    public HodApproval() {}
//
//    public HodApproval(User hod, College college, Branch branch) {
//        this.hod = hod;
//        this.college = college;
//        this.branch = branch;
//        this.status = UserStatus.PENDING;
//    }
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public User getHod() { return hod; }
//    public void setHod(User hod) { this.hod = hod; }
//
//    public College getCollege() { return college; }
//    public void setCollege(College college) { this.college = college; }
//
//    public Branch getBranch() { return branch; }
//    public void setBranch(Branch branch) { this.branch = branch; }
//
//    public UserStatus getStatus() { return status; }
//    public void setStatus(UserStatus status) { this.status = status; }
//
//    public User getDecidedBy() { return decidedBy; }
//    public void setDecidedBy(User decidedBy) { this.decidedBy = decidedBy; }
//
//    public String getDecisionReason() { return decisionReason; }
//    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
//
//    public LocalDateTime getDecidedAt() { return decidedAt; }
//    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//}



package com.polyconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hod_approvals")
public class HodApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hod_id", nullable = false)
    private User hod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING;

    // Same fix as Notification.user / StudentApproval.decidedBy — see those for the full
    // explanation (unsafe lazy proxy over the JOINED-inheritance User base class).
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "decided_by")
    @JsonIgnore
    private User decidedBy;

    @Column(name = "decision_reason")
    private String decisionReason;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public HodApproval() {}

    public HodApproval(User hod, College college, Branch branch) {
        this.hod = hod;
        this.college = college;
        this.branch = branch;
        this.status = UserStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getHod() { return hod; }
    public void setHod(User hod) { this.hod = hod; }

    public College getCollege() { return college; }
    public void setCollege(College college) { this.college = college; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public User getDecidedBy() { return decidedBy; }
    public void setDecidedBy(User decidedBy) { this.decidedBy = decidedBy; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}