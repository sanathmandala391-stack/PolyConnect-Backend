package com.polyconnect.repository;

import com.polyconnect.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentPin(String studentPin);
    List<Attendance> findByCollegeIdAndBranchId(Long collegeId, Long branchId);
    List<Attendance> findByCollegeIdAndBranchIdAndDetentionRiskTrue(Long collegeId, Long branchId);
    List<Attendance> findByDetentionRiskTrue();
}
