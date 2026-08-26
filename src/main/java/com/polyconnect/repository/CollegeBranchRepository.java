package com.polyconnect.repository;

import com.polyconnect.entity.CollegeBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeBranchRepository extends JpaRepository<CollegeBranch, Long> {
    List<CollegeBranch> findByCollegeIdAndActiveTrue(Long collegeId);
    Optional<CollegeBranch> findByCollegeIdAndBranchId(Long collegeId, Long branchId);
    boolean existsByCollegeIdAndBranchId(Long collegeId, Long branchId);
}
