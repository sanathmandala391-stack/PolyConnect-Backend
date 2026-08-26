package com.polyconnect.repository;

import com.polyconnect.entity.Hod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HodRepository extends JpaRepository<Hod, Long> {
    Optional<Hod> findByCollegeIdAndBranchId(Long collegeId, Long branchId);
}
