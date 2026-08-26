//package com.polyconnect.service;
//
//import com.polyconnect.entity.Attendance;
//import com.polyconnect.entity.AttendanceHistory;
//import com.polyconnect.entity.Student;
//import com.polyconnect.exception.ResourceNotFoundException;
//import com.polyconnect.integration.sbtet.SbtetAttendanceSyncService;
//import com.polyconnect.integration.sbtet.SbtetClient;
//import com.polyconnect.repository.AttendanceHistoryRepository;
//import com.polyconnect.repository.AttendanceRepository;
//import com.polyconnect.repository.StudentRepository;
//import com.polyconnect.security.TenantContext;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class AttendanceService {
//
//    private final SbtetClient sbtetClient;
//    private final SbtetAttendanceSyncService syncService;
//    private final AttendanceRepository attendanceRepository;
//    private final AttendanceHistoryRepository attendanceHistoryRepository;
//    private final StudentRepository studentRepository;
//
//    public AttendanceService(
//        SbtetClient sbtetClient,
//        SbtetAttendanceSyncService syncService,
//        AttendanceRepository attendanceRepository,
//        AttendanceHistoryRepository attendanceHistoryRepository,
//        StudentRepository studentRepository
//    ) {
//        this.sbtetClient = sbtetClient;
//        this.syncService = syncService;
//        this.attendanceRepository = attendanceRepository;
//        this.attendanceHistoryRepository = attendanceHistoryRepository;
//        this.studentRepository = studentRepository;
//    }
//
//    public Map<String, Object> getLiveAttendanceReport(String pin) {
//        return sbtetClient.getAttendanceReport(pin);
//    }
//
//    public Attendance getStudentAttendance(String pin) {
//        return attendanceRepository.findByStudentPin(pin)
//            .orElseGet(() -> {
//                Student student = studentRepository.findByPin(pin)
//                    .orElseThrow(() -> new ResourceNotFoundException("Student with PIN " + pin + " not found."));
//                return syncService.syncStudentAttendance(student);
//            });
//    }
//
//    public List<AttendanceHistory> getAttendanceHistory(String pin) {
//        return attendanceHistoryRepository.findByStudentPinOrderBySnapshotDateAsc(pin);
//    }
//
//    public List<Attendance> getDepartmentAttendanceForCurrentHod() {
//        Long collegeId = TenantContext.getRequiredCollegeId();
//        Long branchId = TenantContext.getRequiredBranchId();
//        return attendanceRepository.findByCollegeIdAndBranchId(collegeId, branchId);
//    }
//
//    public List<Attendance> getDepartmentDetentionRisksForCurrentHod() {
//        Long collegeId = TenantContext.getRequiredCollegeId();
//        Long branchId = TenantContext.getRequiredBranchId();
//        return attendanceRepository.findByCollegeIdAndBranchIdAndDetentionRiskTrue(collegeId, branchId);
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
package com.polyconnect.service;

import com.polyconnect.entity.Attendance;
import com.polyconnect.entity.AttendanceHistory;
import com.polyconnect.entity.Student;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.integration.sbtet.SbtetAttendanceSyncService;
import com.polyconnect.integration.sbtet.SbtetClient;
import com.polyconnect.repository.AttendanceHistoryRepository;
import com.polyconnect.repository.AttendanceRepository;
import com.polyconnect.repository.StudentRepository;
import com.polyconnect.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {

    private final SbtetClient sbtetClient;
    private final SbtetAttendanceSyncService syncService;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceHistoryRepository attendanceHistoryRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(
            SbtetClient sbtetClient,
            SbtetAttendanceSyncService syncService,
            AttendanceRepository attendanceRepository,
            AttendanceHistoryRepository attendanceHistoryRepository,
            StudentRepository studentRepository
    ) {
        this.sbtetClient = sbtetClient;
        this.syncService = syncService;
        this.attendanceRepository = attendanceRepository;
        this.attendanceHistoryRepository = attendanceHistoryRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Triggered by "Sync Live Logs" — pulls fresh data from SBTET
     * AND persists it, so the follow-up /attendance read sees it too.
     */
    public Attendance getLiveAttendanceReport(String pin) {
        Student student = studentRepository.findByPin(pin)
                .orElseThrow(() -> new ResourceNotFoundException("Student with PIN " + pin + " not found."));
        return syncService.syncStudentAttendance(student);
    }

    public Attendance getStudentAttendance(String pin) {
        return attendanceRepository.findByStudentPin(pin)
                .orElseGet(() -> {
                    Student student = studentRepository.findByPin(pin)
                            .orElseThrow(() -> new ResourceNotFoundException("Student with PIN " + pin + " not found."));
                    return syncService.syncStudentAttendance(student);
                });
    }

    public List<AttendanceHistory> getAttendanceHistory(String pin) {
        return attendanceHistoryRepository.findByStudentPinOrderBySnapshotDateAsc(pin);
    }

    public List<Attendance> getDepartmentAttendanceForCurrentHod() {
        Long collegeId = TenantContext.getRequiredCollegeId();
        Long branchId = TenantContext.getRequiredBranchId();
        return attendanceRepository.findByCollegeIdAndBranchId(collegeId, branchId);
    }

    public List<Attendance> getDepartmentDetentionRisksForCurrentHod() {
        Long collegeId = TenantContext.getRequiredCollegeId();
        Long branchId = TenantContext.getRequiredBranchId();
        return attendanceRepository.findByCollegeIdAndBranchIdAndDetentionRiskTrue(collegeId, branchId);
    }
}