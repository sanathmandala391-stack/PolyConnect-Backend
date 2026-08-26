package com.polyconnect.repository;

import com.polyconnect.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findBySlug(String slug);
    List<Community> findByCommunityType(String communityType);
    Optional<Community> findByCollegeIdAndCommunityType(Long collegeId, String communityType);
}
