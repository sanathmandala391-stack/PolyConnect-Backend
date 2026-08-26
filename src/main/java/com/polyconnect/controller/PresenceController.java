package com.polyconnect.controller;

import com.polyconnect.security.TenantContext;
import com.polyconnect.service.PresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/online-users")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        Set<Long> userIds = presenceService.getOnlineUserIds();
        Map<String, Object> res = new HashMap<>();
        res.put("onlineUserIds", userIds);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat() {
        Long userId = TenantContext.getCurrentUser().getId();
        presenceService.heartbeat(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("userId", userId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/offline")
    public ResponseEntity<Map<String, Object>> offline() {
        Long userId = TenantContext.getCurrentUser().getId();
        presenceService.markOffline(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    @MessageMapping("/presence/heartbeat")
    public void wsHeartbeat(@Payload Map<String, Object> payload) {
        if (payload != null && payload.get("userId") != null) {
            try {
                Long userId = Long.valueOf(payload.get("userId").toString());
                presenceService.heartbeat(userId);
            } catch (Exception ignored) {}
        }
    }
}