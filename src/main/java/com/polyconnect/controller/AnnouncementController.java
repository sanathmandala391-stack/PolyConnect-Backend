package com.polyconnect.controller;

import com.polyconnect.entity.Announcement;
import com.polyconnect.entity.User;
import com.polyconnect.repository.UserRepository;
import com.polyconnect.security.TenantContext;
import com.polyconnect.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserRepository userRepository;

    public AnnouncementController(AnnouncementService announcementService, UserRepository userRepository) {
        this.announcementService = announcementService;
        this.userRepository = userRepository;
    }

    @GetMapping("/public")
    public ResponseEntity<List<Announcement>> getPublicAnnouncements() {
        return ResponseEntity.ok(announcementService.getPublicAnnouncements());
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<Announcement>> getCollegeAnnouncements(@PathVariable Long collegeId) {
        return ResponseEntity.ok(announcementService.getCollegeAnnouncements(collegeId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'LECTURER')")
    public ResponseEntity<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        User user = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        return ResponseEntity.ok(announcementService.createAnnouncement(announcement, user));
    }
}
