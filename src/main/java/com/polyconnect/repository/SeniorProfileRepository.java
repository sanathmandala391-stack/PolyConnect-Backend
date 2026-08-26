package com.polyconnect.repository;

import com.polyconnect.entity.SeniorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeniorProfileRepository extends JpaRepository<SeniorProfile, Long> {
    Optional<SeniorProfile> findByUserId(Long userId);
    List<SeniorProfile> findByAvailableForMentorshipTrue();
}
