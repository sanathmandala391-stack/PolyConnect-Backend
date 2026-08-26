package com.polyconnect.service;

import com.polyconnect.entity.Announcement;
import com.polyconnect.entity.User;
import com.polyconnect.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<Announcement> getPublicAnnouncements() {
        return announcementRepository.findByTargetScopeOrderByPublishedAtDesc("STATEWIDE");
    }

    public List<Announcement> getCollegeAnnouncements(Long collegeId) {
        return announcementRepository.findByCollegeIdOrderByPublishedAtDesc(collegeId);
    }

    @Transactional
    public Announcement createAnnouncement(Announcement announcementInput, User publishedBy) {
        Announcement announcement = new Announcement();
        announcement.setTitle(announcementInput.getTitle());
        announcement.setContent(announcementInput.getContent());
        announcement.setPriority(announcementInput.getPriority() != null ? announcementInput.getPriority() : "NORMAL");
        announcement.setTargetScope(announcementInput.getTargetScope() != null ? announcementInput.getTargetScope() : "STATEWIDE");
        announcement.setCollege(announcementInput.getCollege());
        announcement.setBranch(announcementInput.getBranch());
        announcement.setPublishedBy(publishedBy);
        announcement.setExpiresAt(announcementInput.getExpiresAt());

        return announcementRepository.save(announcement);
    }
}
