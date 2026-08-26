//package com.polyconnect.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "notifications")
//public class Notification {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Column(nullable = false, length = 200)
//    private String title;
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String message;
//
//    @Column(nullable = false, length = 50)
//    private String type; // ATTENDANCE_ALERT, RESULT_ANNOUNCED, APPROVAL_STATUS, etc.
//
//    @Column(name = "is_read")
//    private Boolean read = false;
//
//    @Column(name = "action_url")
//    private String actionUrl;
//
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    public Notification() {}
//
//    public Notification(User user, String title, String message, String type, String actionUrl) {
//        this.user = user;
//        this.title = title;
//        this.message = message;
//        this.type = type;
//        this.actionUrl = actionUrl;
//        this.read = false;
//    }
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getMessage() { return message; }
//    public void setMessage(String message) { this.message = message; }
//
//    public String getType() { return type; }
//    public void setType(String type) { this.type = type; }
//
//    public Boolean getRead() { return read; }
//    public void setRead(Boolean read) { this.read = read; }
//
//    public String getActionUrl() { return actionUrl; }
//    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//}

package com.polyconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER, not LAZY: User is a JOINED-inheritance base class (Student/Hod extend it), and
    // Hibernate cannot safely build a lazy proxy for a polymorphic JOINED base type without
    // bytecode enhancement (not configured in this project) — that mismatch was throwing an
    // unhandled exception at serialization time, which is the actual root cause of the "An
    // unexpected error occurred" page. EAGER sidesteps the unsafe proxy entirely.
    // @JsonIgnore: the frontend only ever reads title/message/createdAt/read — it never needs
    // the full User (which would otherwise drag in nested College/Branch objects too).
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 50)
    private String type; // ATTENDANCE_ALERT, RESULT_ANNOUNCED, APPROVAL_STATUS, etc.

    @Column(name = "is_read")
    private Boolean read = false;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(User user, String title, String message, String type, String actionUrl) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.actionUrl = actionUrl;
        this.read = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}