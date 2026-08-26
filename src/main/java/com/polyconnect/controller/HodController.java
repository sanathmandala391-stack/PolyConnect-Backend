package com.polyconnect.controller;

import com.polyconnect.entity.Attendance;
import com.polyconnect.entity.Student;
import com.polyconnect.entity.StudentApproval;
import com.polyconnect.repository.StudentRepository;
import com.polyconnect.security.TenantContext;
import com.polyconnect.service.AttendanceService;
import com.polyconnect.service.DashboardService;
import com.polyconnect.service.StudentApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hod")
@PreAuthorize("hasRole('HOD')")
public class HodController {

    private final DashboardService dashboardService;
    private final StudentApprovalService studentApprovalService;
    private final AttendanceService attendanceService;
    private final StudentRepository studentRepository;

    public HodController(
        DashboardService dashboardService,
        StudentApprovalService studentApprovalService,
        AttendanceService attendanceService,
        StudentRepository studentRepository
    ) {
        this.dashboardService = dashboardService;
        this.studentApprovalService = studentApprovalService;
        this.attendanceService = attendanceService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getHodDashboard() {
        return ResponseEntity.ok(dashboardService.getHodDashboard());
    }

    @GetMapping("/approvals/pending")
    public ResponseEntity<List<StudentApproval>> getPendingStudentApprovals() {
        return ResponseEntity.ok(studentApprovalService.getPendingStudentApprovalsForCurrentHod());
    }

    @PostMapping("/approvals/{id}/decision")
    public ResponseEntity<StudentApproval> decideStudentApproval(
        @PathVariable Long id,
        @RequestParam boolean approve,
        @RequestParam(required = false, defaultValue = "") String reason
    ) {
        return ResponseEntity.ok(studentApprovalService.decideStudentApproval(id, approve, reason));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<Attendance>> getDepartmentAttendance() {
        return ResponseEntity.ok(attendanceService.getDepartmentAttendanceForCurrentHod());
    }

    @GetMapping("/attendance/detention-risks")
    public ResponseEntity<List<Attendance>> getDepartmentDetentionRisks() {
        return ResponseEntity.ok(attendanceService.getDepartmentDetentionRisksForCurrentHod());
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getDepartmentStudents() {
        Long collegeId = TenantContext.getRequiredCollegeId();
        Long branchId = TenantContext.getRequiredBranchId();
        return ResponseEntity.ok(studentRepository.findByCollegeIdAndBranchId(collegeId, branchId));
    }
}
