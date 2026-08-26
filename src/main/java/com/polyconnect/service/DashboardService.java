package com.polyconnect.service;

import com.polyconnect.entity.*;
import com.polyconnect.repository.*;
import com.polyconnect.security.TenantContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
    private final AnnouncementRepository announcementRepository;
    private final ReputationRepository reputationRepository;
    private final StudentBadgeRepository studentBadgeRepository;
    private final StudentApprovalRepository studentApprovalRepository;
    private final HodApprovalRepository hodApprovalRepository;

    public DashboardService(
        StudentRepository studentRepository,
        AttendanceRepository attendanceRepository,
        NotificationRepository notificationRepository,
        AnnouncementRepository announcementRepository,
        ReputationRepository reputationRepository,
        StudentBadgeRepository studentBadgeRepository,
        StudentApprovalRepository studentApprovalRepository,
        HodApprovalRepository hodApprovalRepository
    ) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationRepository = notificationRepository;
        this.announcementRepository = announcementRepository;
        this.reputationRepository = reputationRepository;
        this.studentBadgeRepository = studentBadgeRepository;
        this.studentApprovalRepository = studentApprovalRepository;
        this.hodApprovalRepository = hodApprovalRepository;
    }

    /**
     * Complete Student Overview Dashboard: Profile, Dual Attendance %, SGPA/CGPA, Unread Notifications, Badges.
     */
    public Map<String, Object> getStudentDashboard(Student student) {
        Map<String, Object> data = new HashMap<>();
        data.put("student", student);

        attendanceRepository.findByStudentPin(student.getPin()).ifPresent(att -> {
            data.put("attendance", att);
        });

        data.put("unreadNotificationsCount", notificationRepository.countByUserIdAndReadFalse(student.getId()));
        data.put("badges", studentBadgeRepository.findByStudentId(student.getId()));
        reputationRepository.findById(student.getId()).ifPresent(rep -> data.put("reputation", rep));

        // Scoped announcements
        List<Announcement> announcements = announcementRepository.findByTargetScopeOrderByPublishedAtDesc("STATEWIDE");
        if (student.getCollege() != null) {
            announcements.addAll(announcementRepository.findByCollegeIdOrderByPublishedAtDesc(student.getCollege().getId()));
        }
        data.put("recentAnnouncements", announcements);

        return data;
    }

    /**
     * Multi-tenant HOD Dashboard: Department attendance distribution, detention risks, pending approvals.
     */
    public Map<String, Object> getHodDashboard() {
        Long collegeId = TenantContext.getRequiredCollegeId();
        Long branchId = TenantContext.getRequiredBranchId();

        Map<String, Object> data = new HashMap<>();
        List<Student> students = studentRepository.findByCollegeIdAndBranchId(collegeId, branchId);
        List<Attendance> attendances = attendanceRepository.findByCollegeIdAndBranchId(collegeId, branchId);
        List<Attendance> detentionRisks = attendanceRepository.findByCollegeIdAndBranchIdAndDetentionRiskTrue(collegeId, branchId);
        List<StudentApproval> pendingApprovals = studentApprovalRepository.findByCollegeIdAndBranchIdAndStatus(collegeId, branchId, UserStatus.PENDING);

        data.put("totalStudentsCount", students.size());
        data.put("detentionRiskCount", detentionRisks.size());
        data.put("pendingApprovalsCount", pendingApprovals.size());
        data.put("students", students);
        data.put("detentionRisks", detentionRisks);
        data.put("pendingApprovals", pendingApprovals);

        // Calculate department average attendance
        double avg = attendances.stream()
            .mapToDouble(a -> a.getExamEligibilityPercentage() != null ? a.getExamEligibilityPercentage().doubleValue() : 0.0)
            .average().orElse(0.0);
        data.put("departmentAverageAttendance", BigDecimal.valueOf(avg).setScale(2, java.math.RoundingMode.HALF_UP));

        return data;
    }

    /**
     * Admin Overview Dashboard: Total colleges, branches, pending HOD approvals, system stats.
     */
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("pendingHodApprovals", hodApprovalRepository.findByStatus(UserStatus.PENDING));
        data.put("totalStudents", studentRepository.count());
        data.put("totalDetentionRisks", attendanceRepository.findByDetentionRiskTrue().size());
        return data;
    }
}
