package com.polyconnect.repository;

import com.polyconnect.entity.HodApproval;
import com.polyconnect.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HodApprovalRepository extends JpaRepository<HodApproval, Long> {
    List<HodApproval> findByStatus(UserStatus status);
    Optional<HodApproval> findByHodId(Long hodId);
}
