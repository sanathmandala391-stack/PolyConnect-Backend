package com.polyconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.mail.username:${polyconnect.mail.from:sanathmandala391@gmail.com}}")
    private String fromEmail;

    @Value("${polyconnect.mail.brevo-api-key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    @Value("${polyconnect.mail.resend-api-key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendEmail(String to, String subject, String content) {
        if (to == null || to.trim().isEmpty() || !to.contains("@")) {
            log.warn("Invalid email address: {}", to);
            return false;
        }

        String recipient = to.trim();

        // 1. Try Brevo HTTP API (Port 443 - Never blocked on Render)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey.trim());

                Map<String, Object> payload = new HashMap<>();
                payload.put("sender", Map.of("email", fromEmail, "name", "PolyConnect SBTET"));
                payload.put("to", List.of(Map.of("email", recipient)));
                payload.put("subject", subject);
                payload.put("textContent", content);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", request, String.class);
                log.info("Successfully sent email to {} via Brevo API", recipient);
                return true;
            } catch (Exception e) {
                log.warn("Brevo API send failed: {}. Falling back to standard mail sender...", e.getMessage());
            }
        }

        // 2. Try Resend HTTP API (Port 443)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(resendApiKey.trim());

                Map<String, Object> payload = new HashMap<>();
                payload.put("from", "PolyConnect <onboarding@resend.dev>");
                payload.put("to", List.of(recipient));
                payload.put("subject", subject);
                payload.put("text", content);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                restTemplate.postForEntity("https://api.resend.com/emails", request, String.class);
                log.info("Successfully sent email to {} via Resend API", recipient);
                return true;
            } catch (Exception e) {
                log.warn("Resend API send failed: {}. Falling back to standard mail sender...", e.getMessage());
            }
        }

        // 3. Fallback to JavaMailSender (SMTP with SSL Port 465)
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured and no Email API key provided. Cannot send email to {}", recipient);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Successfully sent email notification to {} via SMTP", recipient);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to {} via SMTP: {}", recipient, e.getMessage());
            return false;
        }
    }
}
