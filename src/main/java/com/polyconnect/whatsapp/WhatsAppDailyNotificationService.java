package com.polyconnect.whatsapp;

import com.polyconnect.entity.Role;
import com.polyconnect.entity.User;
import com.polyconnect.entity.UserStatus;
import com.polyconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WhatsAppDailyNotificationService {

    private final UserRepository userRepository;
    private final WhatsAppService whatsAppService;

    @Value("${polyconnect.automation.secret}")
    private String automationSecret;

    public WhatsAppDailyNotificationService(
            UserRepository userRepository,
            WhatsAppService whatsAppService
    ) {
        this.userRepository = userRepository;
        this.whatsAppService = whatsAppService;
    }

    public boolean isValidAutomationSecret(String secret) {
        return secret != null
                && !secret.isBlank()
                && automationSecret != null
                && automationSecret.equals(secret);
    }

    public NotificationResult sendDailyWhatsAppMessages() {

        List<User> students =
                userRepository.findByRoleAndStatusAndWhatsappOptInTrue(
                        Role.STUDENT,
                        UserStatus.APPROVED
                );

        int total = students.size();
        int success = 0;
        int failed = 0;
        int skipped = 0;

        System.out.println("========================================");
        System.out.println("WHATSAPP DAILY NOTIFICATION JOB");
        System.out.println("Eligible students: " + total);
        System.out.println("========================================");

        for (User student : students) {

            String phoneNumber = student.getPhoneNumber();

            if (phoneNumber == null || phoneNumber.isBlank()) {

                skipped++;

                System.out.println(
                        "SKIPPED: "
                                + student.getUsername()
                                + " - no phone number"
                );

                continue;
            }

            try {

                whatsAppService.sendHelloWorldTemplate(phoneNumber);

                success++;

                System.out.println(
                        "SUCCESS: WhatsApp sent to "
                                + student.getUsername()
                );

            } catch (Exception e) {

                failed++;

                System.err.println(
                        "FAILED: WhatsApp for "
                                + student.getUsername()
                                + " - "
                                + e.getMessage()
                );
            }
        }

        System.out.println("========================================");
        System.out.println("WhatsApp job completed");
        System.out.println("Total eligible : " + total);
        System.out.println("Success        : " + success);
        System.out.println("Failed         : " + failed);
        System.out.println("Skipped        : " + skipped);
        System.out.println("========================================");

        return new NotificationResult(
                "SUCCESS",
                total,
                success,
                failed,
                skipped
        );
    }

    public record NotificationResult(
            String status,
            int totalEligible,
            int success,
            int failed,
            int skipped
    ) {
    }
}