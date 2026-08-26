//package com.polyconnect.controller;
//
//import com.polyconnect.entity.HodApproval;
//import com.polyconnect.entity.User;
//import com.polyconnect.repository.UserRepository;
//import com.polyconnect.security.TenantContext;
//import com.polyconnect.service.DashboardService;
//import com.polyconnect.service.HodApprovalService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/admin")
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminController {
//
//    private final DashboardService dashboardService;
//    private final HodApprovalService hodApprovalService;
//    private final UserRepository userRepository;
//
//    public AdminController(DashboardService dashboardService, HodApprovalService hodApprovalService, UserRepository userRepository) {
//        this.dashboardService = dashboardService;
//        this.hodApprovalService = hodApprovalService;
//        this.userRepository = userRepository;
//    }
//
//    @GetMapping("/dashboard")
//    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
//        return ResponseEntity.ok(dashboardService.getAdminDashboard());
//    }
//
//    @GetMapping("/approvals/hod/pending")
//    public ResponseEntity<List<HodApproval>> getPendingHodApprovals() {
//        return ResponseEntity.ok(hodApprovalService.getPendingHodApprovals());
//    }
//
//    @PostMapping("/approvals/hod/{id}/decision")
//    public ResponseEntity<HodApproval> decideHodApproval(
//        @PathVariable Long id,
//        @RequestParam boolean approve,
//        @RequestParam(required = false, defaultValue = "") String reason
//    ) {
//        User admin = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
//        return ResponseEntity.ok(hodApprovalService.decideHodApproval(id, approve, reason, admin));
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//


package com.polyconnect.controller;

import com.polyconnect.entity.*;
import com.polyconnect.repository.*;
import com.polyconnect.security.TenantContext;
import com.polyconnect.service.DashboardService;
import com.polyconnect.service.HodApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    // --- Inner DTO Records ---
    public record StudentAdminDTO(
            Long id,
            String pin,
            String fullName,
            String email,
            String phoneNumber,
            String collegeCode,
            String collegeName,
            String branchCode,
            String branchName,
            String currentSemester,
            Double attendancePercentage,
            String status,
            Boolean active
    ) {}

    public record HodAdminDTO(
            Long id,
            String employeeId,
            String fullName,
            String email,
            String phoneNumber,
            String collegeCode,
            String collegeName,
            String branchCode,
            String branchName,
            String qualification,
            String approvalStatus,
            String status,
            Boolean active
    ) {}

    public record AccessActionRequest(
            String targetType,
            String identifier,
            String email,
            String fullName,
            String reason,
            String category,
            String duration
    ) {}

    // --- Dependencies ---
    private final DashboardService dashboardService;
    private final HodApprovalService hodApprovalService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final HodRepository hodRepository;
    private final CollegeRepository collegeRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@polyconnect.edu.in}")
    private String fromEmail;

    public AdminController(
            DashboardService dashboardService,
            HodApprovalService hodApprovalService,
            UserRepository userRepository,
            StudentRepository studentRepository,
            HodRepository hodRepository,
            CollegeRepository collegeRepository
    ) {
        this.dashboardService = dashboardService;
        this.hodApprovalService = hodApprovalService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.hodRepository = hodRepository;
        this.collegeRepository = collegeRepository;
    }

    // 1. Dashboard Overview
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    // 2. Pending HOD Approvals
    @GetMapping("/approvals/hod/pending")
    public ResponseEntity<List<HodApproval>> getPendingHodApprovals() {
        return ResponseEntity.ok(hodApprovalService.getPendingHodApprovals());
    }

    @PostMapping("/approvals/hod/{id}/decision")
    public ResponseEntity<HodApproval> decideHodApproval(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestParam(required = false, defaultValue = "") String reason
    ) {
        User admin = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        return ResponseEntity.ok(hodApprovalService.decideHodApproval(id, approve, reason, admin));
    }

    // 3. GET All Registered Students
    @GetMapping("/students")
    public ResponseEntity<List<StudentAdminDTO>> getAllStudents() {
        List<StudentAdminDTO> list = studentRepository.findAll().stream().map(s -> {
            boolean isActive = true;
            String status = "ACTIVE";
            var optUser = userRepository.findByUsername(s.getPin());
            if (optUser.isPresent()) {
                isActive = optUser.get().isActive();
                status = isActive ? "ACTIVE" : "SUSPENDED";
            }
            return new StudentAdminDTO(
                    s.getId(),
                    s.getPin(),
                    s.getFullName() != null ? s.getFullName() : s.getPin(),
                    s.getEmail() != null ? s.getEmail() : "",
                    s.getPhoneNumber() != null ? s.getPhoneNumber() : "",
                    s.getCollege() != null ? s.getCollege().getCode() : "",
                    s.getCollege() != null ? s.getCollege().getName() : "",
                    s.getBranch() != null ? s.getBranch().getCode() : "",
                    s.getBranch() != null ? s.getBranch().getName() : "",
                    s.getCurrentSemester() != null ? String.valueOf(s.getCurrentSemester()) : "1",
                    0.0,
                    status,
                    isActive
            );
        }).toList();
        return ResponseEntity.ok(list);
    }

    // 4. GET All Registered HODs
    @GetMapping("/hods")
    public ResponseEntity<List<HodAdminDTO>> getAllHods() {
        List<HodAdminDTO> list = hodRepository.findAll().stream().map(h -> {
            boolean isActive = true;
            String status = "ACTIVE";
            var optUser = userRepository.findByEmail(h.getEmail());
            if (optUser.isPresent()) {
                isActive = optUser.get().isActive();
                status = isActive ? "ACTIVE" : "SUSPENDED";
            }
            return new HodAdminDTO(
                    h.getId(),
                    h.getEmployeeId(),
                    h.getFullName() != null ? h.getFullName() : "Faculty Head",
                    h.getEmail() != null ? h.getEmail() : "",
                    h.getPhoneNumber() != null ? h.getPhoneNumber() : "",
                    h.getCollege() != null ? h.getCollege().getCode() : "",
                    h.getCollege() != null ? h.getCollege().getName() : "",
                    h.getBranch() != null ? h.getBranch().getCode() : "",
                    h.getBranch() != null ? h.getBranch().getName() : "",
                    "",
                    "APPROVED",
                    status,
                    isActive
            );
        }).toList();
        return ResponseEntity.ok(list);
    }

    // 5. GET Incidents
    @GetMapping("/incidents")
    public ResponseEntity<List<Map<String, Object>>> getIncidents() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    // 6. Revoke Access & IMMEDIATELY Send Revocation Notice Email
    @PostMapping("/access/revoke")
    @Transactional
    public ResponseEntity<Map<String, String>> revokeAccess(@RequestBody AccessActionRequest request) {
        String identifier = request.identifier();

        // 1. Deactivate in Database
        userRepository.findByUsername(identifier).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });

        if (request.email() != null && !request.email().isBlank()) {
            userRepository.findByEmail(request.email()).ifPresent(user -> {
                user.setActive(false);
                userRepository.save(user);
            });
        }

        // 2. Resolve Student's Email Address
        String targetEmail = request.email();
        String studentName = request.fullName() != null ? request.fullName() : identifier;

        if (targetEmail == null || targetEmail.isBlank()) {
            var studentOpt = studentRepository.findByPin(identifier);
            if (studentOpt.isPresent() && studentOpt.get().getEmail() != null) {
                targetEmail = studentOpt.get().getEmail();
                studentName = studentOpt.get().getFullName();
            }
        }
        if (targetEmail == null || targetEmail.isBlank()) {
            var userOpt = userRepository.findByUsername(identifier);
            if (userOpt.isPresent() && userOpt.get().getEmail() != null) {
                targetEmail = userOpt.get().getEmail();
            }
        }

        // 3. Dispatch Email Immediately
        if (targetEmail != null && !targetEmail.isBlank()) {
            sendRevocationEmail(targetEmail, studentName, request);
        }

        return ResponseEntity.ok(Map.of("message", "Access revoked and notice emailed to " + identifier));
    }

    // 7. Give Access / Restore & IMMEDIATELY Send Re-instatement Email
    @PostMapping("/access/restore")
    @Transactional
    public ResponseEntity<Map<String, String>> restoreAccess(@RequestBody AccessActionRequest request) {
        String identifier = request.identifier();

        // 1. Activate in Database
        userRepository.findByUsername(identifier).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });

        if (request.email() != null && !request.email().isBlank()) {
            userRepository.findByEmail(request.email()).ifPresent(user -> {
                user.setActive(true);
                userRepository.save(user);
            });
        }

        // 2. Resolve Student's Email Address
        String targetEmail = request.email();
        String studentName = request.fullName() != null ? request.fullName() : identifier;

        if (targetEmail == null || targetEmail.isBlank()) {
            var studentOpt = studentRepository.findByPin(identifier);
            if (studentOpt.isPresent() && studentOpt.get().getEmail() != null) {
                targetEmail = studentOpt.get().getEmail();
                studentName = studentOpt.get().getFullName();
            }
        }
        if (targetEmail == null || targetEmail.isBlank()) {
            var userOpt = userRepository.findByUsername(identifier);
            if (userOpt.isPresent() && userOpt.get().getEmail() != null) {
                targetEmail = userOpt.get().getEmail();
            }
        }

        // 3. Dispatch Restoration Email Immediately
        if (targetEmail != null && !targetEmail.isBlank()) {
            sendRestorationEmail(targetEmail, studentName, request);
        }

        return ResponseEntity.ok(Map.of("message", "Access restored and confirmation email sent to " + identifier));
    }

    // --- Helper 1: Send Disciplinary Revocation Notice Email ---
    private void sendRevocationEmail(String toEmail, String name, AccessActionRequest req) {
        if (mailSender == null) {
            System.err.println("⚠️ JavaMailSender not configured in application.properties");
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(toEmail.trim());
            mail.setSubject(" [URGENT] PolyConnect Portal Access Revoked - SBTET Disciplinary Notice");
            mail.setText("Dear Student (" + name + "),\n\n"
                    + "This is an official administrative notice from the State Board of Technical Education & Training (SBTET), Government of Telangana.\n\n"
                    + "Your access privileges to the PolyConnect Student Portal have been REVOKED due to a disciplinary order.\n\n"
                    + "--------------------------------------------------\n"
                    + "Disciplinary Order Details:\n"
                    + "• Student PIN: " + req.identifier() + "\n"
                    + "• Violation Category: " + req.category() + "\n"
                    + "• Enforcement Duration: " + req.duration() + "\n"
                    + "• Disciplinary Reason: " + req.reason() + "\n"
                    + "--------------------------------------------------\n\n"
                    + "You are currently barred from logging into the portal, downloading hall tickets, and accessing examination services.\n\n"
                    + "Please contact your Department HOD or College Principal immediately for inquiry proceedings.\n\n"
                    + "Regards,\n"
                    + "Controller of Examinations & Administration\n"
                    + "State Board of Technical Education & Training (SBTET), Telangana");

            mailSender.send(mail);
            System.out.println("✅ Disciplinary Revocation Email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send revocation email to " + toEmail + ": " + e.getMessage());
        }
    }

    // --- Helper 2: Send Re-instatement / Access Restored Email ---
    private void sendRestorationEmail(String toEmail, String name, AccessActionRequest req) {
        if (mailSender == null) {
            System.err.println("⚠️ JavaMailSender not configured in application.properties");
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(toEmail.trim());
            mail.setSubject(" [APPROVED] PolyConnect Portal Access Restored - You Can Now Access Your Account");
            mail.setText("Dear Student (" + name + "),\n\n"
                    + "This is an official administrative notice from the State Board of Technical Education & Training (SBTET), Government of Telangana.\n\n"
                    + "Your access privileges to the PolyConnect Student Portal have been successfully RESTORED by the State Board Administrator.\n\n"
                    + "--------------------------------------------------\n"
                    + "Re-instatement Details:\n"
                    + "• Student PIN: " + req.identifier() + "\n"
                    + "• Privilege Status: ACTIVE / AUTHORIZED\n"
                    + "• Clearance Remarks: " + (req.reason() != null && !req.reason().isBlank() ? req.reason() : "Formal inquiry resolved and clearance issued.") + "\n"
                    + "--------------------------------------------------\n\n"
                    + "You can now log into your account, download hall tickets, and access all student services normally.\n\n"
                    + "Sign in here:\n"
                    + "http://localhost:5173/login\n\n"
                    + "Regards,\n"
                    + "Controller of Examinations & Administration\n"
                    + "State Board of Technical Education & Training (SBTET), Telangana");

            mailSender.send(mail);
            System.out.println("✅ Access Restored Email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send restore email to " + toEmail + ": " + e.getMessage());
        }
    }
}