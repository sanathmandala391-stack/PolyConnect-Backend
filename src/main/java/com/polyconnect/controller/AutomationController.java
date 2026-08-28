package com.polyconnect.controller;

import com.polyconnect.service.AttendanceAutomationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/automation")
public class AutomationController {

    private final AttendanceAutomationService attendanceAutomationService;

    @Value("${polyconnect.automation.secret:${automation.secret:}}")
    private String automationSecret;

    public AutomationController(AttendanceAutomationService attendanceAutomationService) {
        this.attendanceAutomationService = attendanceAutomationService;
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> runAttendanceAutomation(
            @RequestHeader(value = "X-Automation-Secret", required = false) String headerSecret,
            @RequestParam(value = "secret", required = false) String paramSecret) {

        String providedSecret = headerSecret != null ? headerSecret : paramSecret;

        // If an automation secret is configured, enforce matching
        if (automationSecret != null && !automationSecret.isBlank()) {
            if (providedSecret == null || !providedSecret.trim().equals(automationSecret.trim())) {
                return ResponseEntity.status(401).body(Map.of(
                        "status", "UNAUTHORIZED",
                        "message", "Invalid or missing X-Automation-Secret"
                ));
            }
        }

        // Process attendance and return email payloads to GitHub Actions
        AttendanceAutomationService.AttendanceAutomationResult result =
                attendanceAutomationService.processAndPrepareEmails();

        return ResponseEntity.ok(result);
    }
}
