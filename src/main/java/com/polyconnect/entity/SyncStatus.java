package com.polyconnect.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_status")
public class SyncStatus {

    @Id
    private String id; // fixed value like "sbtet_sync"

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public SyncStatus() {}

    public SyncStatus(String id, LocalDateTime lastSyncedAt) {
        this.id = id;
        this.lastSyncedAt = lastSyncedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}