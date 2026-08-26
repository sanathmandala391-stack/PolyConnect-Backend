//package com.polyconnect.controller;
//
//import com.polyconnect.entity.Attendance;
//import com.polyconnect.entity.AttendanceHistory;
//import com.polyconnect.entity.Student;
//import com.polyconnect.repository.StudentRepository;
//import com.polyconnect.security.TenantContext;
//import com.polyconnect.service.AttendanceService;
//import com.polyconnect.service.DashboardService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/student")
//@PreAuthorize("hasRole('STUDENT')")
//public class StudentController {
//
//    private final DashboardService dashboardService;
//    private final AttendanceService attendanceService;
//    private final StudentRepository studentRepository;
//
//    public StudentController(
//        DashboardService dashboardService,
//        AttendanceService attendanceService,
//        StudentRepository studentRepository
//    ) {
//        this.dashboardService = dashboardService;
//        this.attendanceService = attendanceService;
//        this.studentRepository = studentRepository;
//    }
//
//    @GetMapping("/dashboard")
//    public ResponseEntity<Map<String, Object>> getStudentDashboard() {
//        Long studentId = TenantContext.getCurrentUser().getId();
//        Student student = studentRepository.findById(studentId).orElseThrow();
//        return ResponseEntity.ok(dashboardService.getStudentDashboard(student));
//    }
//
//    @GetMapping("/attendance")
//    public ResponseEntity<Attendance> getAttendance() {
//        String pin = TenantContext.getCurrentUser().getUsername();
//        return ResponseEntity.ok(attendanceService.getStudentAttendance(pin));
//    }
//
//    @GetMapping("/attendance/history")
//    public ResponseEntity<List<AttendanceHistory>> getAttendanceHistory() {
//        String pin = TenantContext.getCurrentUser().getUsername();
//        return ResponseEntity.ok(attendanceService.getAttendanceHistory(pin));
//    }
//
//    @GetMapping("/attendance/live")
//    public ResponseEntity<Map<String, Object>> getLiveAttendance() {
//        String pin = TenantContext.getCurrentUser().getUsername();
//        return ResponseEntity.ok(attendanceService.getLiveAttendanceReport(pin));
//    }
//}







































package com.polyconnect.controller;

import com.polyconnect.entity.Attendance;
import com.polyconnect.entity.AttendanceHistory;
import com.polyconnect.entity.Student;
import com.polyconnect.repository.StudentRepository;
import com.polyconnect.security.TenantContext;
import com.polyconnect.service.AttendanceService;
import com.polyconnect.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final DashboardService dashboardService;
    private final AttendanceService attendanceService;
    private final StudentRepository studentRepository;

    public StudentController(
            DashboardService dashboardService,
            AttendanceService attendanceService,
            StudentRepository studentRepository
    ) {
        this.dashboardService = dashboardService;
        this.attendanceService = attendanceService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getStudentDashboard() {
        Long studentId = TenantContext.getCurrentUser().getId();
        Student student = studentRepository.findById(studentId).orElseThrow();
        return ResponseEntity.ok(dashboardService.getStudentDashboard(student));
    }

    @GetMapping("/attendance")
    public ResponseEntity<Attendance> getAttendance() {
        String pin = TenantContext.getCurrentUser().getUsername();
        return ResponseEntity.ok(attendanceService.getStudentAttendance(pin));
    }

    @GetMapping("/attendance/history")
    public ResponseEntity<List<AttendanceHistory>> getAttendanceHistory() {
        String pin = TenantContext.getCurrentUser().getUsername();
        return ResponseEntity.ok(attendanceService.getAttendanceHistory(pin));
    }

    @GetMapping("/attendance/live")
    public ResponseEntity<Attendance> getLiveAttendance() {
        String pin = TenantContext.getCurrentUser().getUsername();
        return ResponseEntity.ok(attendanceService.getLiveAttendanceReport(pin));
    }
}