package com.polyconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);
    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(35);

    private final Map<Long, Instant> lastSeenMap = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void heartbeat(Long userId) {
        if (userId == null) return;
        boolean wasOffline = !isUserOnline(userId);
        lastSeenMap.put(userId, Instant.now());

        if (wasOffline) {
            broadcastPresence(userId, true);
        }
    }

    public void markOffline(Long userId) {
        if (userId == null) return;
        lastSeenMap.remove(userId);
        broadcastPresence(userId, false);
    }

    public boolean isUserOnline(Long userId) {
        if (userId == null) return false;
        Instant lastSeen = lastSeenMap.get(userId);
        if (lastSeen == null) return false;
        if (Duration.between(lastSeen, Instant.now()).compareTo(ONLINE_TIMEOUT) > 0) {
            lastSeenMap.remove(userId);
            return false;
        }
        return true;
    }

    public Set<Long> getOnlineUserIds() {
        Instant now = Instant.now();
        Set<Long> online = new HashSet<>();
        for (Map.Entry<Long, Instant> entry : lastSeenMap.entrySet()) {
            if (Duration.between(entry.getValue(), now).compareTo(ONLINE_TIMEOUT) <= 0) {
                online.add(entry.getKey());
            } else {
                lastSeenMap.remove(entry.getKey());
            }
        }
        return online;
    }

    private void broadcastPresence(Long userId, boolean isOnline) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("isOnline", isOnline);
            payload.put("timestamp", Instant.now().toEpochMilli());
            messagingTemplate.convertAndSend("/topic/presence", payload);
        } catch (Exception e) {
            log.error("Failed to broadcast presence for user #{}: {}", userId, e.getMessage());
        }
    }
}