package com.polyconnect.security;

import com.polyconnect.entity.Role;
import com.polyconnect.entity.User;
import com.polyconnect.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final Role role;
    private final UserStatus status;
    private final Long collegeId;
    private final String collegeCode;
    private final Long branchId;
    private final String branchCode;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String password, Role role, UserStatus status,
                         Long collegeId, String collegeCode, Long branchId, String branchCode,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.collegeId = collegeId;
        this.collegeCode = collegeCode;
        this.branchId = branchId;
        this.branchCode = branchCode;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Long collegeId = user.getCollege() != null ? user.getCollege().getId() : null;
        String collegeCode = user.getCollege() != null ? user.getCollege().getCode() : null;
        Long branchId = user.getBranch() != null ? user.getBranch().getId() : null;
        String branchCode = user.getBranch() != null ? user.getBranch().getCode() : null;

        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.getStatus(),
            collegeId,
            collegeCode,
            branchId,
            branchCode,
            authorities
        );
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public Long getCollegeId() { return collegeId; }
    public String getCollegeCode() { return collegeCode; }
    public Long getBranchId() { return branchId; }
    public String getBranchCode() { return branchCode; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return status != UserStatus.SUSPENDED && status != UserStatus.REJECTED; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return status == UserStatus.APPROVED; }
}
