//package com.polyconnect.integration.sbtet;
//
//import com.polyconnect.entity.Attendance;
//import com.polyconnect.entity.AttendanceHistory;
//import com.polyconnect.entity.Notification;
//import com.polyconnect.entity.Student;
//import com.polyconnect.repository.AttendanceHistoryRepository;
//import com.polyconnect.repository.AttendanceRepository;
//import com.polyconnect.repository.NotificationRepository;
//import com.polyconnect.repository.StudentRepository;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class SbtetAttendanceSyncService {
//
//    private final SbtetClient sbtetClient;
//    private final StudentRepository studentRepository;
//    private final AttendanceRepository attendanceRepository;
//    private final AttendanceHistoryRepository attendanceHistoryRepository;
//    private final NotificationRepository notificationRepository;
//
//    public SbtetAttendanceSyncService(
//        SbtetClient sbtetClient,
//        StudentRepository studentRepository,
//        AttendanceRepository attendanceRepository,
//        AttendanceHistoryRepository attendanceHistoryRepository,
//        NotificationRepository notificationRepository
//    ) {
//        this.sbtetClient = sbtetClient;
//        this.studentRepository = studentRepository;
//        this.attendanceRepository = attendanceRepository;
//        this.attendanceHistoryRepository = attendanceHistoryRepository;
//        this.notificationRepository = notificationRepository;
//    }
//
//    /**
//     * Nightly attendance synchronization job for all registered students.
//     */
//    @Scheduled(cron = "${polyconnect.sbtet.sync-cron:0 0 2 * * ?}")
//    @Transactional
//    public void syncAllStudentsAttendance() {
//        List<Student> students = studentRepository.findAll();
//        for (Student student : students) {
//            try {
//                syncStudentAttendance(student);
//            } catch (Exception ignored) {
//                // Log and continue with next student
//            }
//        }
//    }
//
//    @Transactional
//    public Attendance syncStudentAttendance(Student student) {
//        Map<String, Object> data = sbtetClient.getAttendanceReport(student.getPin());
//
//        Attendance attendance = attendanceRepository.findByStudentPin(student.getPin())
//            .orElseGet(() -> {
//                Attendance att = new Attendance();
//                att.setStudentPin(student.getPin());
//                return att;
//            });
//
//        if (student.getCollege() != null) attendance.setCollegeId(student.getCollege().getId());
//        if (student.getBranch() != null) attendance.setBranchId(student.getBranch().getId());
//
//        int workingDays = (int) data.getOrDefault("workingDays", 0);
//        int presentDays = (int) data.getOrDefault("presentDays", 0);
//        int absentDays = (int) data.getOrDefault("absentDays", 0);
//        int examsWorkingDays = (int) data.getOrDefault("examsWorkingDays", workingDays);
//        BigDecimal currentStanding = (BigDecimal) data.getOrDefault("currentStandingPercentage", BigDecimal.ZERO);
//        BigDecimal examEligibility = (BigDecimal) data.getOrDefault("examEligibilityPercentage", BigDecimal.ZERO);
//        boolean isRisk = (boolean) data.getOrDefault("isDetentionRisk", false);
//
//        attendance.setWorkingDays(workingDays);
//        attendance.setPresentDays(presentDays);
//        attendance.setAbsentDays(absentDays);
//        attendance.setExamsWorkingDays(examsWorkingDays);
//        attendance.setCurrentStandingPercentage(currentStanding);
//        attendance.setExamEligibilityPercentage(examEligibility);
//        attendance.setDetentionRisk(isRisk);
//        attendance.setLastSyncedAt(LocalDateTime.now());
//
//        Attendance savedAttendance = attendanceRepository.save(attendance);
//
//        // Update student cached standing and detention risk
//        student.setCurrentStandingPercentage(currentStanding);
//        student.setExamEligibilityPercentage(examEligibility);
//        student.setDetentionRisk(isRisk);
//        studentRepository.save(student);
//
//        // Record history snapshot for trend graphs
//        LocalDate today = LocalDate.now();
//        if (!attendanceHistoryRepository.existsByStudentPinAndSnapshotDate(student.getPin(), today)) {
//            AttendanceHistory history = new AttendanceHistory(
//                student.getPin(),
//                today,
//                currentStanding,
//                examEligibility,
//                presentDays,
//                workingDays,
//                examsWorkingDays
//            );
//            attendanceHistoryRepository.save(history);
//        }
//
//        // Trigger detention alert notification if below 75%
//        if (isRisk) {
//            Notification alert = new Notification(
//                student,
//                "Attendance Detention Risk Alert",
//                "Your exam eligibility attendance is " + examEligibility + "%, which is below the mandatory 75% SBTET threshold.",
//                "ATTENDANCE_ALERT",
//                "/student/attendance"
//            );
//            notificationRepository.save(alert);
//        }
//
//        return savedAttendance;
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




















































package com.polyconnect.integration.sbtet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polyconnect.entity.Attendance;
import com.polyconnect.entity.AttendanceHistory;
import com.polyconnect.entity.Notification;
import com.polyconnect.entity.Student;
import com.polyconnect.repository.AttendanceHistoryRepository;
import com.polyconnect.repository.AttendanceRepository;
import com.polyconnect.repository.NotificationRepository;
import com.polyconnect.repository.StudentRepository;
import com.polyconnect.service.SyncStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SbtetAttendanceSyncService {

    private static final Logger log = LoggerFactory.getLogger(SbtetAttendanceSyncService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SyncStatusService syncStatusService;
    private final SbtetClient sbtetClient;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceHistoryRepository attendanceHistoryRepository;
    private final NotificationRepository notificationRepository;

    public SbtetAttendanceSyncService(
            SbtetClient sbtetClient,
            StudentRepository studentRepository,
            AttendanceRepository attendanceRepository,
            AttendanceHistoryRepository attendanceHistoryRepository,
            NotificationRepository notificationRepository
    ) {
        this.sbtetClient = sbtetClient;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceHistoryRepository = attendanceHistoryRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "${polyconnect.sbtet.sync-cron:0 0 6 * * ?}")
    @Transactional
    public void syncAllStudentsAttendance() {
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            try {
                syncStudentAttendance(student);
            } catch (Exception e) {
                log.error("Attendance sync failed for PIN {}: {}", student.getPin(), e.getMessage(), e);
            }
        }
        syncStatusService.markSyncCompletedNow();
    }







//    @Transactional
//    public Attendance syncStudentAttendance(Student student) {
//        Map<String, Object> data = sbtetClient.getAttendanceReport(student.getPin());
//
//        Object dailyRecordsRaw = data.get("dailyRecords");
//        if (dailyRecordsRaw instanceof JsonNode table1Node && table1Node.isArray()) {
//            Map<String, String> dailyMap = new LinkedHashMap<>();
//            for (JsonNode day : table1Node) {
//                String month = day.path("AttendanceMonth").asText("");
//                String dayNum = day.path("Day").asText("");
//                String status = day.path("Status").asText("-");
//                if (!month.isBlank() && !dayNum.isBlank()) {
//                    dailyMap.put(month + "-" + dayNum, status);
//                }
//            }
//            try {
//                attendance.setDailyRecordsJson(objectMapper.writeValueAsString(dailyMap));
//            } catch (Exception e) {
//                log.error("Failed to serialize daily records for PIN {}: {}", student.getPin(), e.getMessage());
//            }
//        }
//
//        Attendance attendance = attendanceRepository.findByStudentPin(student.getPin())
//                .orElseGet(() -> {
//                    Attendance att = new Attendance();
//                    att.setStudentPin(student.getPin());
//                    return att;
//                });
//
//        if (student.getCollege() != null) attendance.setCollegeId(student.getCollege().getId());
//        if (student.getBranch() != null) attendance.setBranchId(student.getBranch().getId());
//
//        int workingDays = (int) data.getOrDefault("workingDays", 0);
//        int presentDays = (int) data.getOrDefault("presentDays", 0);
//        int absentDays = (int) data.getOrDefault("absentDays", 0);
//        int examsWorkingDays = (int) data.getOrDefault("examsWorkingDays", workingDays);
//        BigDecimal currentStanding = (BigDecimal) data.getOrDefault("currentStandingPercentage", BigDecimal.ZERO);
//        BigDecimal examEligibility = (BigDecimal) data.getOrDefault("examEligibilityPercentage", BigDecimal.ZERO);
//        boolean isRisk = (boolean) data.getOrDefault("isDetentionRisk", false);
//
//        attendance.setWorkingDays(workingDays);
//        attendance.setPresentDays(presentDays);
//        attendance.setAbsentDays(absentDays);
//        attendance.setExamsWorkingDays(examsWorkingDays);
//        attendance.setCurrentStandingPercentage(currentStanding);
//        attendance.setExamEligibilityPercentage(examEligibility);
//        attendance.setDetentionRisk(isRisk);
//        attendance.setLastSyncedAt(LocalDateTime.now());
//
//        Attendance savedAttendance = attendanceRepository.save(attendance);
//
//        student.setCurrentStandingPercentage(currentStanding);
//        student.setExamEligibilityPercentage(examEligibility);
//        student.setDetentionRisk(isRisk);
//        studentRepository.save(student);
//
//        LocalDate today = LocalDate.now();
//        if (!attendanceHistoryRepository.existsByStudentPinAndSnapshotDate(student.getPin(), today)) {
//            AttendanceHistory history = new AttendanceHistory(
//                    student.getPin(),
//                    today,
//                    currentStanding,
//                    examEligibility,
//                    presentDays,
//                    workingDays,
//                    examsWorkingDays
//            );
//            attendanceHistoryRepository.save(history);
//        }
//
//        if (isRisk) {
//            Notification alert = new Notification(
//                    student,
//                    "Attendance Detention Risk Alert",
//                    "Your exam eligibility attendance is " + examEligibility + "%, which is below the mandatory 75% SBTET threshold.",
//                    "ATTENDANCE_ALERT",
//                    "/student/attendance"
//            );
//            notificationRepository.save(alert);
//        }
//
//        return savedAttendance;
//    }















































    @Transactional
    public Attendance syncStudentAttendance(Student student) {
        Map<String, Object> data = sbtetClient.getAttendanceReport(student.getPin());

        Attendance attendance = attendanceRepository.findByStudentPin(student.getPin())
                .orElseGet(() -> {
                    Attendance att = new Attendance();
                    att.setStudentPin(student.getPin());
                    return att;
                });

        if (student.getCollege() != null) attendance.setCollegeId(student.getCollege().getId());
        if (student.getBranch() != null) attendance.setBranchId(student.getBranch().getId());

        int workingDays = (int) data.getOrDefault("workingDays", 0);
        int presentDays = (int) data.getOrDefault("presentDays", 0);
        int absentDays = (int) data.getOrDefault("absentDays", 0);
        int examsWorkingDays = (int) data.getOrDefault("examsWorkingDays", workingDays);
        BigDecimal currentStanding = (BigDecimal) data.getOrDefault("currentStandingPercentage", BigDecimal.ZERO);
        BigDecimal examEligibility = (BigDecimal) data.getOrDefault("examEligibilityPercentage", BigDecimal.ZERO);
        boolean isRisk = (boolean) data.getOrDefault("isDetentionRisk", false);

        attendance.setWorkingDays(workingDays);
        attendance.setPresentDays(presentDays);
        attendance.setAbsentDays(absentDays);
        attendance.setExamsWorkingDays(examsWorkingDays);
        attendance.setCurrentStandingPercentage(currentStanding);
        attendance.setExamEligibilityPercentage(examEligibility);
        attendance.setDetentionRisk(isRisk);
        attendance.setLastSyncedAt(LocalDateTime.now());

        // ---- daily records: now `attendance` exists, so this compiles ----
        Object dailyRecordsRaw = data.get("dailyRecords");
        if (dailyRecordsRaw instanceof JsonNode table1Node && table1Node.isArray()) {
            Map<String, String> dailyMap = new LinkedHashMap<>();
            for (JsonNode day : table1Node) {
                String month = day.path("AttendanceMonth").asText("");
                String dayNum = day.path("Day").asText("");
                String status = day.path("Status").asText("-");
                if (!month.isBlank() && !dayNum.isBlank()) {
                    dailyMap.put(month + "-" + dayNum, status);
                }
            }
            try {
                attendance.setDailyRecordsJson(objectMapper.writeValueAsString(dailyMap));
            } catch (Exception e) {
                log.error("Failed to serialize daily records for PIN {}: {}", student.getPin(), e.getMessage());
            }
        }
        // --------------------------------------------------------------------

        Attendance savedAttendance = attendanceRepository.save(attendance);

        student.setCurrentStandingPercentage(currentStanding);
        student.setExamEligibilityPercentage(examEligibility);
        student.setDetentionRisk(isRisk);
        studentRepository.save(student);

        LocalDate today = LocalDate.now();
        if (!attendanceHistoryRepository.existsByStudentPinAndSnapshotDate(student.getPin(), today)) {
            AttendanceHistory history = new AttendanceHistory(
                    student.getPin(),
                    today,
                    currentStanding,
                    examEligibility,
                    presentDays,
                    workingDays,
                    examsWorkingDays
            );
            attendanceHistoryRepository.save(history);
        }

        if (isRisk) {
            Notification alert = new Notification(
                    student,
                    "Attendance Detention Risk Alert",
                    "Your exam eligibility attendance is " + examEligibility + "%, which is below the mandatory 75% SBTET threshold.",
                    "ATTENDANCE_ALERT",
                    "/student/attendance"
            );
            notificationRepository.save(alert);
        }

        return savedAttendance;
    }
}