package com.polyconnect.service;

import com.polyconnect.entity.HodApproval;
import com.polyconnect.entity.User;
import com.polyconnect.entity.UserStatus;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.repository.HodApprovalRepository;
import com.polyconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HodApprovalService {

    private final HodApprovalRepository hodApprovalRepository;
    private final UserRepository userRepository;

    public HodApprovalService(HodApprovalRepository hodApprovalRepository, UserRepository userRepository) {
        this.hodApprovalRepository = hodApprovalRepository;
        this.userRepository = userRepository;
    }

    public List<HodApproval> getPendingHodApprovals() {
        return hodApprovalRepository.findByStatus(UserStatus.PENDING);
    }

    @Transactional
    public HodApproval decideHodApproval(Long approvalId, boolean approve, String reason, User adminUser) {
        HodApproval approval = hodApprovalRepository.findById(approvalId)
            .orElseThrow(() -> new ResourceNotFoundException("HOD Approval request #" + approvalId + " not found."));

        UserStatus newStatus = approve ? UserStatus.APPROVED : UserStatus.REJECTED;
        approval.setStatus(newStatus);
        approval.setDecidedBy(adminUser);
        approval.setDecisionReason(reason);
        approval.setDecidedAt(LocalDateTime.now());

        // Update user status
        User hod = approval.getHod();
        hod.setStatus(newStatus);
        userRepository.save(hod);

        return hodApprovalRepository.save(approval);
    }
}
