package com.polyconnect.controller;


import com.polyconnect.service.AttendanceAutomationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/automation")
public class AutomationController {

    private final AttendanceAutomationService attendanceAutomationService;

    @Value("${automation.secret}")
    private String automationSecret;

    public AutomationController(
            AttendanceAutomationService attendanceAutomationService) {
        this.attendanceAutomationService = attendanceAutomationService;
    }

    @PostMapping("/attendance")
    public ResponseEntity<String> runAttendanceAutomation(
            @RequestHeader(value = "X-Automation-Secret", required = false) String secret) {

        if (secret == null || !secret.equals(automationSecret)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        attendanceAutomationService.runDailyAttendanceCheck();

        return ResponseEntity.ok("Attendance automation completed");
    }
}