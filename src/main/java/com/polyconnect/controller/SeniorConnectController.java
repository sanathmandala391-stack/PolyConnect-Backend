package com.polyconnect.controller;

import com.polyconnect.entity.ChatMessage;
import com.polyconnect.entity.ChatRoom;
import com.polyconnect.entity.SeniorProfile;
import com.polyconnect.entity.User;
import com.polyconnect.repository.ChatRoomRepository;
import com.polyconnect.repository.UserRepository;
import com.polyconnect.security.TenantContext;
import com.polyconnect.service.EmailService;
import com.polyconnect.service.NotificationService;
import com.polyconnect.service.SeniorConnectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seniors")
public class SeniorConnectController {

    private final SeniorConnectService seniorConnectService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public SeniorConnectController(
        SeniorConnectService seniorConnectService,
        UserRepository userRepository,
        ChatRoomRepository chatRoomRepository,
        EmailService emailService,
        NotificationService notificationService
    ) {
        this.seniorConnectService = seniorConnectService;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @GetMapping("/mentors")
    public ResponseEntity<List<SeniorProfile>> getMentors() {
        Long currentUserId = TenantContext.getCurrentUser().getId();
        return ResponseEntity.ok(seniorConnectService.getAvailableMentors(currentUserId));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<SeniorProfile> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(seniorConnectService.getSeniorProfile(userId));
    }

    @PostMapping("/profile")
    public ResponseEntity<SeniorProfile> saveProfile(@RequestBody SeniorProfile profile) {
        User user = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        return ResponseEntity.ok(seniorConnectService.saveSeniorProfile(profile, user));
    }

    @PostMapping("/chat/start")
    public ResponseEntity<ChatRoom> startChat(@RequestBody Map<String, Object> body) {
        Long mentorId = Long.valueOf(body.get("mentorId").toString());
        String topic = (String) body.get("topic");

        User student = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        User mentor = userRepository.findById(mentorId).orElseThrow();

        return ResponseEntity.ok(seniorConnectService.getOrCreateChatRoom(mentor, student, topic));
    }

    @GetMapping("/chat/rooms")
    public ResponseEntity<List<ChatRoom>> getMyChatRooms() {
        Long userId = TenantContext.getCurrentUser().getId();
        return ResponseEntity.ok(seniorConnectService.getUserChatRooms(userId));
    }

    @GetMapping("/chat/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getRoomMessages(@PathVariable Long roomId) {
        Long currentUserId = TenantContext.getCurrentUser().getId();
        return ResponseEntity.ok(seniorConnectService.getMessagesAndMarkRead(roomId, currentUserId));
    }

    @PostMapping("/chat/rooms/{roomId}/read")
    public ResponseEntity<Map<String, Object>> markRoomMessagesRead(@PathVariable Long roomId) {
        Long currentUserId = TenantContext.getCurrentUser().getId();
        seniorConnectService.getMessagesAndMarkRead(roomId, currentUserId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/chat/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessage> sendMessage(
        @PathVariable Long roomId,
        @RequestBody Map<String, String> body
    ) {
        User sender = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        return ResponseEntity.ok(seniorConnectService.sendMessage(roomId, sender, body.get("content")));
    }

    @PostMapping("/chat/rooms/{roomId}/notify-offline")
    public ResponseEntity<Map<String, Object>> notifyOfflineRoomMentor(
        @PathVariable Long roomId,
        @RequestBody Map<String, String> body
    ) {
        User student = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        User mentor = room.getMentor();

        String subject = body.getOrDefault("subject", "PolyConnect: You have a session to solve the Doubts");
        String message = body.getOrDefault("message", 
            "Respected " + mentor.getFullName() + ",\n\n" +
            "Student " + student.getFullName() + " has initiated a doubt solving session with you on PolyConnect.\n" +
            "Topic: " + (room.getTopic() != null ? room.getTopic() : "Academic Mentorship") + "\n\n" +
            "Please log in to PolyConnect at your earliest convenience to respond to the student.\n\n" +
            "Regards,\nPolyConnect Mentorship Cell\nState Board of Technical Education & Training (SBTET)"
        );

        String recipientEmail = mentor.getEmail();
        if (body.containsKey("recipientEmail") && body.get("recipientEmail") != null && !body.get("recipientEmail").isBlank()) {
            recipientEmail = body.get("recipientEmail");
        }

        boolean emailSent = false;
        if (recipientEmail != null && !recipientEmail.isBlank()) {
            emailSent = emailService.sendEmail(recipientEmail, subject, message);
        }

        try {
            notificationService.sendNotification(
                mentor,
                subject,
                "Student " + student.getFullName() + " requested a doubt session in Room #" + roomId + " (" + room.getTopic() + ")",
                "MENTOR_SESSION_REQUEST",
                "/student/seniors/chat/" + roomId
            );
        } catch (Exception ignored) {}

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("emailSent", emailSent);
        res.put("recipient", recipientEmail);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/mentors/{mentorId}/notify-offline")
    public ResponseEntity<Map<String, Object>> notifyOfflineMentorDirect(
        @PathVariable Long mentorId,
        @RequestBody Map<String, String> body
    ) {
        User student = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        User mentor = userRepository.findById(mentorId).orElseThrow();

        String subject = body.getOrDefault("subject", "PolyConnect: You have a session to solve the Doubts (Academic Mentorship)");
        String message = body.getOrDefault("message",
            "Respected " + mentor.getFullName() + ",\n\n" +
            "Student " + student.getFullName() + " has requested a mentorship session on PolyConnect Senior Connect.\n\n" +
            "Please log in to PolyConnect to connect with the student.\n\n" +
            "Regards,\nPolyConnect Mentorship Cell\nState Board of Technical Education & Training (SBTET)"
        );

        String recipientEmail = mentor.getEmail();
        if (body.containsKey("recipientEmail") && body.get("recipientEmail") != null && !body.get("recipientEmail").isBlank()) {
            recipientEmail = body.get("recipientEmail");
        }

        boolean emailSent = false;
        if (recipientEmail != null && !recipientEmail.isBlank()) {
            emailSent = emailService.sendEmail(recipientEmail, subject, message);
        }

        try {
            notificationService.sendNotification(
                mentor,
                subject,
                "Student " + student.getFullName() + " requested a doubt solving session on PolyConnect",
                "MENTOR_SESSION_REQUEST",
                "/student/seniors"
            );
        } catch (Exception ignored) {}

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("emailSent", emailSent);
        res.put("recipient", recipientEmail);
        return ResponseEntity.ok(res);
    }
}