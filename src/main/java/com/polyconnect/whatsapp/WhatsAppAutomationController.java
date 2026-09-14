package com.polyconnect.whatsapp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/automation/whatsapp")
public class WhatsAppAutomationController {

    private final WhatsAppDailyNotificationService notificationService;

    public WhatsAppAutomationController(
            WhatsAppDailyNotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    /**
     * Daily WhatsApp automation endpoint.
     *
     * Called by external Cron Job every morning at 6:00 AM.
     *
     * POST /api/automation/whatsapp/daily
     */
    @PostMapping("/daily")
    public ResponseEntity<?> sendDailyWhatsAppMessages(
            @RequestHeader(
                    value = "X-Automation-Secret",
                    required = false
            )
            String secret
    ) {

        // Security check
        if (!notificationService.isValidAutomationSecret(secret)) {

            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "Forbidden"
                    ));
        }

        try {

            WhatsAppDailyNotificationService.NotificationResult result =
                    notificationService.sendDailyWhatsAppMessages();

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "WhatsApp daily job failed"
                    ));
        }
    }
}
