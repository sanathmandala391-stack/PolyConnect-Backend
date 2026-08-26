package com.polyconnect.repository;

import com.polyconnect.entity.User;
import com.polyconnect.entity.Role;
import com.polyconnect.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRoleAndStatus(Role role, UserStatus status);
    List<User> findByCollegeIdAndBranchIdAndRole(Long collegeId, Long branchId, Role role);
}
