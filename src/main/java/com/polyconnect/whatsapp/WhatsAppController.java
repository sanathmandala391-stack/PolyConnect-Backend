package com.polyconnect.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @Value("${polyconnect.whatsapp.verify-token}")
    private String verifyToken;

    public WhatsAppController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    /**
     * Meta webhook verification
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge
    ) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.status(403).body("Forbidden");
    }

    /**
     * WhatsApp webhook events
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String payload
    ) {

        System.out.println("========== WHATSAPP WEBHOOK ==========");
        System.out.println(payload);
        System.out.println("======================================");

        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    /**
     * Test normal text message.
     *
     * POST /api/whatsapp/test
     */
    @PostMapping("/test")
    public ResponseEntity<?> testMessage(
            @RequestBody Map<String, String> request
    ) {

        String phoneNumber = request.get("phoneNumber");
        String message = request.get("message");

        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "phoneNumber is required"
                    ));
        }

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "message is required"
                    ));
        }

        try {

            return ResponseEntity.ok(
                    whatsAppService.sendTextMessage(
                            phoneNumber,
                            message
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "status", "ERROR",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Test the approved WhatsApp template.
     *
     * POST /api/whatsapp/test-template
     *
     * JSON:
     * {
     *   "phoneNumber": "919876543210"
     * }
     */
    @PostMapping("/test-template")
    public ResponseEntity<?> testTemplate(
            @RequestBody Map<String, String> request
    ) {

        String phoneNumber = request.get("phoneNumber");

        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "phoneNumber is required"
                    ));
        }

        try {

            return ResponseEntity.ok(
                    whatsAppService.sendDailyAttendanceTemplate(
                            phoneNumber,
                            "Sana",
                            "72.5",
                            "↓ 2.1%",
                            "⚠️ Your attendance is below 75%.",
                            "3"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "status", "ERROR",
                            "message", e.getMessage()
                    ));
        }
    }
}