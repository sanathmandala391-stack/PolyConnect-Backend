package com.polyconnect.dto;


public record AccessActionRequest(
        String targetType,       // "STUDENT" or "HOD"
        String identifier,       // PIN or Employee ID
        String reason,           // Incident / Malpractice Reason
        String category,         // "EXAM_MALPRACTICE", etc.
        String duration          // "INDEFINITE_REVOCATION", etc.
) {}
