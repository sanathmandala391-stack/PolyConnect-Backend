package com.polyconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot attendance automation service.
 * Fetches biometric attendance from SBTET API, stores daily snapshots in DB,
 * and prepares email payloads for GitHub Actions to dispatch via Gmail.
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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class AttendanceEmailPayload {
        private String to;
        private String studentName;
        private String pin;
        private String subject;
        private String body;

        public AttendanceEmailPayload() {}

        public AttendanceEmailPayload(String to, String studentName, String pin, String subject, String body) {
            this.to = to;
            this.studentName = studentName;
            this.pin = pin;
            this.subject = subject;
            this.body = body;
        }

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    public static class AttendanceAutomationResult {
        private String status;
        private int totalProcessed;
        private int emailsCount;
        private List<AttendanceEmailPayload> emails;

        public AttendanceAutomationResult(String status, int totalProcessed, List<AttendanceEmailPayload> emails) {
            this.status = status;
            this.totalProcessed = totalProcessed;
            this.emailsCount = emails != null ? emails.size() : 0;
            this.emails = emails != null ? emails : new ArrayList<>();
        }

        public String getStatus() { return status; }
        public int getTotalProcessed() { return totalProcessed; }
        public int getEmailsCount() { return emailsCount; }
        public List<AttendanceEmailPayload> getEmails() { return emails; }
    }

    /**
     * Processes attendance from SBTET for all approved students, updates the database,
     * and returns the prepared email list for GitHub Actions to send directly.
     */
    public AttendanceAutomationResult processAndPrepareEmails() {
        System.out.println(">>> AttendanceAutomationService: job FIRED at " + java.time.LocalDateTime.now());

        List<String> pins = jdbcTemplate.queryForList(
                "SELECT username FROM users WHERE role = 'STUDENT' AND status = 'APPROVED'",
                String.class
        );

        System.out.println(">>> Found " + pins.size() + " approved student PIN(s): " + pins);

        List<AttendanceEmailPayload> emailList = new ArrayList<>();
        int processedCount = 0;

        for (String pin : pins) {
            try {
                System.out.println(">>> Processing PIN: " + pin);
                AttendanceEmailPayload emailPayload = processStudent(pin);
                if (emailPayload != null) {
                    emailList.add(emailPayload);
                }
                System.out.println(">>> Finished PIN: " + pin);
            } catch (Exception e) {
                System.err.println("Attendance check failed for PIN " + pin + ": " + e.getMessage());
            }
            processedCount++;
        }

        System.out.println(String.format(
                ">>> AttendanceAutomationService: completed. Total: %d, Email Payloads Prepared: %d",
                processedCount, emailList.size()
        ));

        return new AttendanceAutomationResult("SUCCESS", processedCount, emailList);
    }

    private AttendanceEmailPayload processStudent(String pin) throws Exception {
        // 1. Call SBTET attendance API
        String rawJson;
        try {
            rawJson = restTemplate.getForObject(SBTET_URL + pin, String.class);
        } catch (Exception httpErr) {
            System.err.println("    [" + pin + "] Failed to contact SBTET API: " + httpErr.getMessage());
            return null;
        }

        System.out.println("    [" + pin + "] SBTET raw response: " + rawJson);

        if (rawJson == null || rawJson.isBlank() || rawJson.contains("Divide by zero error encountered")) {
            System.out.println("    [" + pin + "] STOPPED: SBTET returned a non-JSON / error response");
            return null;
        }

        // 2. Parse response safely
        JsonNode outer;
        try {
            outer = objectMapper.readTree(rawJson);
        } catch (Exception parseErr) {
            System.out.println("    [" + pin + "] STOPPED: Failed to parse raw response as JSON");
            return null;
        }

        JsonNode root;
        if (outer.isTextual()) {
            String inner = outer.asText();
            try {
                root = objectMapper.readTree(inner);
            } catch (Exception parseError) {
                System.out.println("    [" + pin + "] STOPPED: SBTET returned non-JSON inner string: " + inner);
                return null;
            }
        } else {
            root = outer;
        }

        JsonNode tableItem = root.path("Table").isArray() && root.path("Table").size() > 0
                ? root.path("Table").get(0)
                : objectMapper.createObjectNode();

        if (tableItem.isMissingNode() || tableItem.isEmpty()) {
            System.out.println("    [" + pin + "] STOPPED: no Table data returned from SBTET");
            return null;
        }

        // 3. Extract fields
        String name = tableItem.path("Name").asText("Student");
        double currentStanding = tableItem.hasNonNull("Percentage")
                ? tableItem.path("Percentage").asDouble(0.0)
                : tableItem.path("TotalPercentage").asDouble(0.0);
        double examEligibility = tableItem.hasNonNull("ExamsPer")
                ? tableItem.path("ExamsPer").asDouble(0.0)
                : (tableItem.hasNonNull("TotalPercentage") ? tableItem.path("TotalPercentage").asDouble(currentStanding) : currentStanding);

        int examsPresentDays = tableItem.path("ExamsNDP").asInt(0);
        int examsWorkingDays = tableItem.path("ExamsWorkingDays").asInt(180);
        int numberOfDaysPresent = tableItem.path("NumberOfDaysPresent").asInt(0);
        int workingDays = tableItem.path("WorkingDays").asInt(180);

        System.out.println("    [" + pin + "] Parsed: name=" + name + " currentStanding=" + currentStanding + " examEligibility=" + examEligibility);

        // 4. Query previous snapshot
        Double previousPercentage = null;
        try {
            previousPercentage = jdbcTemplate.query(
                    "SELECT exam_eligibility_percentage FROM attendance_history " +
                            "WHERE student_pin = ? ORDER BY snapshot_date DESC, created_at DESC LIMIT 1",
                    rs -> rs.next() ? rs.getDouble(1) : null,
                    pin
            );
        } catch (Exception e) {
            System.out.println("    [" + pin + "] Could not fetch previous snapshot: " + e.getMessage());
        }

        boolean hasPreviousRecord = previousPercentage != null;
        Double percentageChange = hasPreviousRecord
                ? Math.round((examEligibility - previousPercentage) * 100.0) / 100.0
                : null;

        // 5. Prediction logic based on Exam Eligibility
        Integer classesNeeded = null;
        String eligibilityStatus = "safe";
        if (examEligibility < TARGET_PERCENTAGE) {
            double targetDecimal = TARGET_PERCENTAGE / 100.0;
            if (targetDecimal < 1.0) {
                double raw = (targetDecimal * examsWorkingDays - examsPresentDays) / (1 - targetDecimal);
                classesNeeded = (int) Math.ceil(Math.max(0, raw));
            }
            eligibilityStatus = examEligibility < CRITICAL_THRESHOLD ? "critical" : "warning";
        }

        // 6. Save today's snapshot to database (recorded in MySQL)
        try {
            jdbcTemplate.update(
                    "INSERT INTO attendance_history " +
                            "(student_pin, snapshot_date, current_standing_percentage, exam_eligibility_percentage, " +
                            "present_days, working_days, exams_working_days, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())",
                    pin, LocalDate.now(), currentStanding, examEligibility,
                    numberOfDaysPresent, workingDays, examsWorkingDays
            );
            System.out.println("    [" + pin + "] Attendance snapshot saved to attendance_history");
        } catch (Exception dbEx) {
            System.err.println("    [" + pin + "] Failed to insert attendance snapshot: " + dbEx.getMessage());
        }

        // Also synchronize main attendance table in MySQL
        try {
            boolean isRisk = examEligibility < TARGET_PERCENTAGE;
            jdbcTemplate.update(
                    "UPDATE attendance SET " +
                            "working_days = ?, " +
                            "present_days = ?, " +
                            "absent_days = ?, " +
                            "exams_working_days = ?, " +
                            "current_standing_percentage = ?, " +
                            "exam_eligibility_percentage = ?, " +
                            "is_detention_risk = ?, " +
                            "last_synced_at = NOW(), " +
                            "raw_summary_json = ? " +
                            "WHERE student_pin = ?",
                    workingDays,
                    numberOfDaysPresent,
                    Math.max(0, workingDays - numberOfDaysPresent),
                    examsWorkingDays,
                    currentStanding,
                    examEligibility,
                    isRisk,
                    tableItem.toString(),
                    pin
            );
            System.out.println("    [" + pin + "] Updated main attendance table in MySQL");
        } catch (Exception dbEx) {
            System.err.println("    [" + pin + "] Failed to update attendance table: " + dbEx.getMessage());
        }

        // 7. Lookup student email
        String studentEmail = null;
        try {
            List<String> emails = jdbcTemplate.queryForList(
                    "SELECT email FROM users WHERE username = ? AND role = 'STUDENT' LIMIT 1",
                    String.class,
                    pin
            );
            if (!emails.isEmpty()) {
                studentEmail = emails.get(0);
            }
        } catch (Exception e) {
            System.err.println("    [" + pin + "] Failed to query email: " + e.getMessage());
        }

        System.out.println("    [" + pin + "] Email lookup result: " + studentEmail);

        if (studentEmail == null || studentEmail.isBlank()) {
            System.out.println("    [" + pin + "] STOPPED: no email on file");
            return null;
        }

        // 8. Build message & return payload
        String message = buildAlertMessage(name, examEligibility, currentStanding,
                numberOfDaysPresent, workingDays, examsPresentDays, examsWorkingDays,
                percentageChange, hasPreviousRecord, classesNeeded, eligibilityStatus);

        return new AttendanceEmailPayload(
                studentEmail,
                name,
                pin,
                "PolyConnect Attendance Update - " + name,
                message
        );
    }

    private String buildAlertMessage(String name, double examEligibility, double currentStanding,
                                     int numberOfDaysPresent, int workingDays, int examsPresentDays, int examsWorkingDays,
                                     Double percentageChange, boolean hasPreviousRecord, Integer classesNeeded, String status) {
        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(name).append(",\n\n");
        message.append("Here is your daily attendance update from SBTET PolyConnect:\n\n");
        message.append("• Exam Eligibility Attendance: ").append(String.format("%.2f", examEligibility)).append("%\n");
        message.append("• Current Academic Standing: ").append(String.format("%.2f", currentStanding)).append("% (").append(numberOfDaysPresent).append("/").append(workingDays).append(" days present)\n");
        message.append("• Total Exam Present Days: ").append(examsPresentDays).append("/").append(examsWorkingDays).append(" days\n");

        if (hasPreviousRecord && percentageChange != null) {
            String direction = percentageChange < 0 ? "decreased by" : percentageChange > 0 ? "increased by" : "unchanged";
            message.append("• Change since last check: ").append(direction)
                    .append(" ").append(Math.abs(percentageChange)).append("%\n");
        }

        if (examEligibility < TARGET_PERCENTAGE) {
            message.append("\n⚠️ ALERT: Your exam eligibility attendance is below 75%.\n");
            if (classesNeeded != null && classesNeeded > 0) {
                message.append("• Classes Needed: You need approximately ").append(classesNeeded)
                        .append(" consecutive classes to reach the mandatory 75% examination threshold.\n");
            }
            if ("critical".equals(status)) {
                message.append("\n🛑 CRITICAL WARNING: At your current attendance rate, you are at risk of detention / exam ineligibility.\n");
            }
        } else {
            message.append("\n✅ You are currently ELIGIBLE for examinations (Attendance ≥ 75%). Keep up the good work!\n");
        }

        message.append("\nTo view your complete 31-day biometric attendance sheet, visit the PolyConnect portal.\n\n");
        message.append("Regards,\nState Board of Technical Education and Training (SBTET), Telangana");

        return message.toString();
    }
}
