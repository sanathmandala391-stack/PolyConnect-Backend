package com.polyconnect.repository;

import com.polyconnect.entity.Student;
import com.polyconnect.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByPin(String pin);
    boolean existsByPin(String pin);

    // Multi-tenant scoped queries for HOD
    List<Student> findByCollegeIdAndBranchId(Long collegeId, Long branchId);
    List<Student> findByCollegeIdAndBranchIdAndStatus(Long collegeId, Long branchId, UserStatus status);
    List<Student> findByCollegeIdAndBranchIdAndDetentionRiskTrue(Long collegeId, Long branchId);

    @Query("SELECT s FROM Student s WHERE s.college.id = :collegeId AND s.branch.id = :branchId AND s.status = 'APPROVED'")
    List<Student> findApprovedStudentsByDepartment(@Param("collegeId") Long collegeId, @Param("branchId") Long branchId);
}
