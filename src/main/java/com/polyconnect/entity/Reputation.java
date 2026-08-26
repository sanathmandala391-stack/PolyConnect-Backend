//package com.polyconnect.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "reputation")
//public class Reputation {
//
//    @Id
//    private Long id; // Same as user id
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @MapsId
//    @JoinColumn(name = "id")
//    private User user;
//
//    @Column(nullable = false)
//    private Integer points = 0;
//
//    @Column(nullable = false)
//    private Integer level = 1;
//
//    @Column(name = "helpful_answers")
//    private Integer helpfulAnswers = 0;
//
//    @Column(name = "posts_created")
//    private Integer postsCreated = 0;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt = LocalDateTime.now();
//
//    public Reputation() {}
//
//    public Reputation(User user) {
//        this.user = user;
//        this.points = 10;
//        this.level = 1;
//    }
//
//    @PreUpdate
//    public void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//        this.level = 1 + (this.points / 100);
//    }
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//
//    public Integer getPoints() { return points; }
//    public void setPoints(Integer points) {
//        this.points = points;
//        this.level = 1 + (points / 100);
//    }
//
//    public Integer getLevel() { return level; }
//    public void setLevel(Integer level) { this.level = level; }
//
//    public Integer getHelpfulAnswers() { return helpfulAnswers; }
//    public void setHelpfulAnswers(Integer helpfulAnswers) { this.helpfulAnswers = helpfulAnswers; }
//
//    public Integer getPostsCreated() { return postsCreated; }
//    public void setPostsCreated(Integer postsCreated) { this.postsCreated = postsCreated; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//}









package com.polyconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reputation")
public class Reputation {

    @Id
    private Long id; // Same as user id

    // Same fix as Notification.user — see there for the full explanation (unsafe lazy proxy
    // over the JOINED-inheritance User base class). The student dashboard only ever reads
    // reputation.points, never reputation.user, so JsonIgnore is safe here too.
    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(name = "helpful_answers")
    private Integer helpfulAnswers = 0;

    @Column(name = "posts_created")
    private Integer postsCreated = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Reputation() {}

    public Reputation(User user) {
        this.user = user;
        this.points = 10;
        this.level = 1;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.level = 1 + (this.points / 100);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) {
        this.points = points;
        this.level = 1 + (points / 100);
    }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getHelpfulAnswers() { return helpfulAnswers; }
    public void setHelpfulAnswers(Integer helpfulAnswers) { this.helpfulAnswers = helpfulAnswers; }

    public Integer getPostsCreated() { return postsCreated; }
    public void setPostsCreated(Integer postsCreated) { this.postsCreated = postsCreated; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}