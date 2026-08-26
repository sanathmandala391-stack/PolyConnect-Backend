package com.polyconnect.dto;


public record StudentAdminDTO(
        Long id,
        String pin,
        String fullName,
        String email,
        String phoneNumber,
        String collegeCode,
        String collegeName,
        String branchCode,
        String branchName,
        String currentSemester,
        Double attendancePercentage,
        String status,        // "ACTIVE" or "SUSPENDED"
        Boolean active
) {}
