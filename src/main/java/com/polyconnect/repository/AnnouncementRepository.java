package com.polyconnect.repository;

import com.polyconnect.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByTargetScopeOrderByPublishedAtDesc(String targetScope);
    List<Announcement> findByCollegeIdOrderByPublishedAtDesc(Long collegeId);
    List<Announcement> findByCollegeIdAndBranchIdOrderByPublishedAtDesc(Long collegeId, Long branchId);
}
