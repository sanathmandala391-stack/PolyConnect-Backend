package com.polyconnect.service;


import com.polyconnect.entity.SyncStatus;
import com.polyconnect.repository.SyncStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SyncStatusService {

    private static final String SBTET_SYNC_ID = "sbtet_sync";

    @Autowired
    private SyncStatusRepository syncStatusRepository;

    public LocalDateTime getLastSyncTimestamp() {
        return syncStatusRepository.findById(SBTET_SYNC_ID)
                .map(SyncStatus::getLastSyncedAt)
                .orElse(null);
    }

    public void markSyncCompletedNow() {
        SyncStatus status = syncStatusRepository.findById(SBTET_SYNC_ID)
                .orElse(new SyncStatus(SBTET_SYNC_ID, null));
        status.setLastSyncedAt(LocalDateTime.now());
        syncStatusRepository.save(status);
    }
}