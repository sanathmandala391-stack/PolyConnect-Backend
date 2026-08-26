package com.polyconnect.security;

import com.polyconnect.exception.TenantAccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class TenantContext {

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new TenantAccessDeniedException("Unauthenticated access - no active tenant context.");
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    public static Long getRequiredCollegeId() {
        UserPrincipal user = getCurrentUser();
        if (user.getCollegeId() == null) {
            throw new TenantAccessDeniedException("User is not associated with any college tenant.");
        }
        return user.getCollegeId();
    }

    public static Long getRequiredBranchId() {
        UserPrincipal user = getCurrentUser();
        if (user.getBranchId() == null) {
            throw new TenantAccessDeniedException("User is not associated with any branch tenant.");
        }
        return user.getBranchId();
    }

    public static void validateHodScope(Long requestedCollegeId, Long requestedBranchId) {
        UserPrincipal user = getCurrentUser();
        if (user.getRole() == com.polyconnect.entity.Role.ADMIN) {
            return; // Admins have global cross-tenant access
        }

        if (requestedCollegeId != null && !requestedCollegeId.equals(user.getCollegeId())) {
            throw new TenantAccessDeniedException("Cross-college access forbidden. Your assigned college ID is " + user.getCollegeId());
        }

        if (requestedBranchId != null && !requestedBranchId.equals(user.getBranchId())) {
            throw new TenantAccessDeniedException("Cross-department access forbidden. Your assigned branch ID is " + user.getBranchId());
        }
    }
}
