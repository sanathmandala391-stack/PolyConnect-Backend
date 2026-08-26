package com.polyconnect.repository;

import com.polyconnect.entity.StudentApproval;
import com.polyconnect.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentApprovalRepository extends JpaRepository<StudentApproval, Long> {
    // Multi-tenant isolation for HOD
    List<StudentApproval> findByCollegeIdAndBranchIdAndStatus(Long collegeId, Long branchId, UserStatus status);
    List<StudentApproval> findByCollegeIdAndBranchId(Long collegeId, Long branchId);
    Optional<StudentApproval> findByStudentId(Long studentId);
}
