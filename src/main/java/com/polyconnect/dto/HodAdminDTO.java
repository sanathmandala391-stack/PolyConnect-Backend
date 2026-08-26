package com.polyconnect.dto;


public record HodAdminDTO(
        Long id,
        String employeeId,
        String fullName,
        String email,
        String phoneNumber,
        String collegeCode,
        String collegeName,
        String branchCode,
        String branchName,
        String qualification,
        String approvalStatus,
        String status,         // "ACTIVE" or "SUSPENDED"
        Boolean active
) {}

