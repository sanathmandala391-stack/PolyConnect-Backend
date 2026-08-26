package com.polyconnect.service;

import com.polyconnect.entity.StudentApproval;
import com.polyconnect.entity.User;
import com.polyconnect.entity.UserStatus;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.repository.StudentApprovalRepository;
import com.polyconnect.repository.UserRepository;
import com.polyconnect.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentApprovalService {

    private final StudentApprovalRepository studentApprovalRepository;
    private final UserRepository userRepository;

    public StudentApprovalService(StudentApprovalRepository studentApprovalRepository, UserRepository userRepository) {
        this.studentApprovalRepository = studentApprovalRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves pending student registration approvals strictly scoped to authenticated HOD's college and branch.
     */
    public List<StudentApproval> getPendingStudentApprovalsForCurrentHod() {
        Long collegeId = TenantContext.getRequiredCollegeId();
        Long branchId = TenantContext.getRequiredBranchId();

        return studentApprovalRepository.findByCollegeIdAndBranchIdAndStatus(collegeId, branchId, UserStatus.PENDING);
    }

    @Transactional
    public StudentApproval decideStudentApproval(Long approvalId, boolean approve, String reason) {
        StudentApproval approval = studentApprovalRepository.findById(approvalId)
            .orElseThrow(() -> new ResourceNotFoundException("Student Approval request #" + approvalId + " not found."));

        // Multi-tenant isolation check: verify that this student belongs to the HOD's department
        TenantContext.validateHodScope(approval.getCollege().getId(), approval.getBranch().getId());

        User currentHod = TenantContext.getCurrentUser() != null ?
            userRepository.findById(TenantContext.getCurrentUser().getId()).orElse(null) : null;

        UserStatus newStatus = approve ? UserStatus.APPROVED : UserStatus.REJECTED;
        approval.setStatus(newStatus);
        approval.setDecidedBy(currentHod);
        approval.setDecisionReason(reason);
        approval.setDecidedAt(LocalDateTime.now());

        // Update student account status
        User student = approval.getStudent();
        student.setStatus(newStatus);
        userRepository.save(student);

        return studentApprovalRepository.save(approval);
    }
}
