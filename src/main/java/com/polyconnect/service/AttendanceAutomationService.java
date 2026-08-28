package com.polyconnect.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Direct Spring Boot replacement for the n8n "My workflow 4" attendance automation.
 * Runs daily at 6:00 AM. No n8n, no separate hosting needed for this job —
 * it runs inside whatever process already hosts the PolyConnect backend.
 */
@Service
public class AttendanceAutomationService {

    private static final String SBTET_URL =
            "https://www.sbtet.telangana.gov.in/api/api/PreExamination/getAttendanceReport?Pin=";

    private static final double TARGET_PERCENTAGE = 75.0;
    private static final double DROP_ALERT_THRESHOLD = 70.0;
    private static final double CRITICAL_THRESHOLD = 65.0;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JavaMailSender mailSender;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${polyconnect.mail.from:sanathmandala391@gmail.com}")
    private String fromEmail;

    /**
     * Equivalent of the n8n Schedule Trigger node: cron "0 6 * * *"
     */
//    @Scheduled(cron = "0 0 6 * * *") // TEMP: testing at 12:28 PM — change back to "0 0 6 * * *" after
    public void runDailyAttendanceCheck() {
        System.out.println(">>> AttendanceAutomationService: scheduled job FIRED at " + java.time.LocalDateTime.now());

        // Step 1 — equivalent of "Execute a SQL query3": get all approved student PINs
        List<String> pins = jdbcTemplate.queryForList(
                "SELECT username FROM users WHERE role = 'STUDENT' AND status = 'APPROVED'",
                String.class
        );

        System.out.println(">>> Found " + pins.size() + " approved student PIN(s): " + pins);

        for (String pin : pins) {
            try {
                System.out.println(">>> Processing PIN: " + pin);
                processStudent(pin);
                System.out.println(">>> Finished PIN: " + pin);
            } catch (Exception e) {
                // Log and continue — one student's failure shouldn't stop the batch
                e.printStackTrace();
                System.err.println("Attendance check failed for PIN " + pin + ": " + e.getMessage());
            }
        }

        System.out.println(">>> AttendanceAutomationService: scheduled job COMPLETE");
    }

    private void processStudent(String pin) throws Exception {
        // Step 2 — equivalent of "HTTP Request": call SBTET attendance API
        String rawJson = restTemplate.getForObject(SBTET_URL + pin, String.class);
        System.out.println("    [" + pin + "] SBTET raw response: " + rawJson);

        // SBTET double-encodes its response: the HTTP body is itself a JSON STRING
        // containing the real JSON (or sometimes a plain error string like
        // "Divide by zero error encountered."). Parse once to unwrap the outer
        // string, then parse again to get the actual object — same as the n8n
        // Code node's "typeof $json.data === 'string' ? JSON.parse(...) : $json".
        JsonNode outer = objectMapper.readTree(rawJson);
        JsonNode root;
        if (outer.isTextual()) {
            String inner = outer.asText();
            try {
                root = objectMapper.readTree(inner);
            } catch (Exception parseError) {
                // Not JSON at all — e.g. "Divide by zero error encountered."
                System.out.println("    [" + pin + "] STOPPED: SBTET returned a non-JSON error: " + inner);
                return;
            }
        } else {
            root = outer;
        }

        JsonNode tableItem = root.path("Table").isArray() && root.path("Table").size() > 0
                ? root.path("Table").get(0)
                : objectMapper.createObjectNode();

        if (tableItem.isMissingNode() || tableItem.isEmpty()) {
            System.out.println("    [" + pin + "] STOPPED: no Table data returned from SBTET");
            return; // no data returned for this student today
        }

        // Step 3 — equivalent of "Code in JavaScript": extract fields
        String name = tableItem.path("Name").asText(null);
        double totalPercentage = tableItem.path("TotalPercentage").asDouble(0);
        int examsPresentDays = tableItem.path("ExamsNDP").asInt(0);
        int examsWorkingDays = tableItem.path("ExamsWorkingDays").asInt(0);
        int numberOfDaysPresent = tableItem.path("NumberOfDaysPresent").asInt(0);
        int workingDays = tableItem.path("WorkingDays").asInt(0);

        System.out.println("    [" + pin + "] Parsed: name=" + name + " totalPercentage=" + totalPercentage);

        // Step 4 — equivalent of "Execute a SQL query": get previous snapshot
        Double previousPercentage = jdbcTemplate.query(
                "SELECT exam_eligibility_percentage FROM attendance_history " +
                        "WHERE student_pin = ? ORDER BY snapshot_date DESC LIMIT 1",
                rs -> rs.next() ? rs.getDouble(1) : null,
                pin
        );

        boolean hasPreviousRecord = previousPercentage != null;
        Double percentageChange = hasPreviousRecord
                ? Math.round((totalPercentage - previousPercentage) * 100.0) / 100.0
                : null;

        // Step 5 — equivalent of "Code in JavaScript2": prediction logic
        Integer classesNeeded = null;
        String eligibilityStatus = "safe";
        if (totalPercentage < TARGET_PERCENTAGE) {
            double targetDecimal = TARGET_PERCENTAGE / 100.0;
            double raw = (targetDecimal * examsWorkingDays - examsPresentDays) / (1 - targetDecimal);
            classesNeeded = (int) Math.ceil(raw);
            eligibilityStatus = totalPercentage < CRITICAL_THRESHOLD ? "critical" : "warning";
        }

        // Step 6 — equivalent of "Code in JavaScript3": build the alert message
        String message = buildAlertMessage(name, totalPercentage, percentageChange,
                hasPreviousRecord, classesNeeded, eligibilityStatus);

        // Step 7 — equivalent of "Execute a SQL query2": get student email
        Map<String, Object> userRow = jdbcTemplate.queryForMap(
                "SELECT email FROM users WHERE username = ? AND role = 'STUDENT' LIMIT 1",
                pin
        );
        String studentEmail = userRow != null ? (String) userRow.get("email") : null;

        System.out.println("    [" + pin + "] Email lookup result: " + studentEmail);

        if (studentEmail == null || studentEmail.isBlank()) {
            System.out.println("    [" + pin + "] STOPPED: no email on file");
            return; // no email on file, skip sending
        }

        // Step 8 — equivalent of "Send an Email"
        System.out.println("    [" + pin + "] Sending email to: " + studentEmail);
        sendEmail(studentEmail, name, message);
        System.out.println("    [" + pin + "] Email sent successfully");

        // Step 9 — save today's snapshot (not present in the uploaded n8n export,
        // but needed so tomorrow's "previous percentage" comparison has data)
        jdbcTemplate.update(
                "INSERT INTO attendance_history " +
                        "(student_pin, snapshot_date, current_standing_percentage, exam_eligibility_percentage, " +
                        "present_days, working_days, exams_working_days, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())",
                pin, LocalDate.now(), totalPercentage, totalPercentage,
                numberOfDaysPresent, workingDays, examsWorkingDays
        );
    }

    private String buildAlertMessage(String name, double totalPercentage, Double percentageChange,
                                     boolean hasPreviousRecord, Integer classesNeeded, String status) {
        StringBuilder message = new StringBuilder();
        message.append("Attendance Update for ").append(name).append("\n\n");
        message.append("Today's Attendance: ").append(totalPercentage).append("%\n");

        if (hasPreviousRecord && percentageChange != null) {
            String direction = percentageChange < 0 ? "decreased by" : percentageChange > 0 ? "increased by" : "unchanged";
            message.append("Change since last check: ").append(direction)
                    .append(" ").append(Math.abs(percentageChange)).append("%\n");
        }

        if (totalPercentage < DROP_ALERT_THRESHOLD) {
            message.append("\nYour attendance dropped below 70%.\n");
        }

        if (classesNeeded != null) {
            message.append("You need approximately ").append(classesNeeded)
                    .append(" more classes to reach 75%.\n");
        }

        if ("critical".equals(status)) {
            message.append("\nAt your current attendance rate, you may become ineligible for exams.\n");
        }

        return message.toString();
    }

    private void sendEmail(String toEmail, String studentName, String body) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("PolyConnect Attendance Update — " + studentName);
        mailMessage.setText(body);
        mailSender.send(mailMessage);
    }
}
