package com.polyconnect.service;

import com.polyconnect.entity.ChatMessage;
import com.polyconnect.entity.ChatRoom;
import com.polyconnect.entity.SeniorProfile;
import com.polyconnect.entity.User;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.repository.ChatMessageRepository;
import com.polyconnect.repository.ChatRoomRepository;
import com.polyconnect.repository.SeniorProfileRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeniorConnectService {

    private final SeniorProfileRepository seniorProfileRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SeniorConnectService(
        SeniorProfileRepository seniorProfileRepository,
        ChatRoomRepository chatRoomRepository,
        ChatMessageRepository chatMessageRepository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.seniorProfileRepository = seniorProfileRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<SeniorProfile> getAvailableMentors(Long currentUserId) {
        return seniorProfileRepository.findAll().stream()
                .filter(p -> p.getUser() != null && !p.getUser().getId().equals(currentUserId))
                .toList();
    }

    public SeniorProfile getSeniorProfile(Long userId) {
        return seniorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Senior profile for user #" + userId + " not found."));
    }

    @Transactional
    public SeniorProfile saveSeniorProfile(SeniorProfile profileInput, User user) {
        SeniorProfile profile = seniorProfileRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                SeniorProfile p = new SeniorProfile();
                p.setUser(user);
                return p;
            });

        profile.setCurrentCompanyOrCollege(profileInput.getCurrentCompanyOrCollege());
        profile.setDesignation(profileInput.getDesignation());
        profile.setEcetRank(profileInput.getEcetRank());
        profile.setDiplomaScorePercentage(profileInput.getDiplomaScorePercentage());
        profile.setSkills(profileInput.getSkills());
        profile.setBio(profileInput.getBio());
        profile.setLinkedinUrl(profileInput.getLinkedinUrl());
        profile.setAvailableForMentorship(profileInput.getAvailableForMentorship());

        return seniorProfileRepository.save(profile);
    }

    @Transactional
    public ChatRoom getOrCreateChatRoom(User mentor, User student, String topic) {
        return chatRoomRepository.findByMentorIdAndStudentId(mentor.getId(), student.getId())
            .orElseGet(() -> {
                ChatRoom room = new ChatRoom(mentor, student, topic != null ? topic : "Mentorship Guidance");
                return chatRoomRepository.save(room);
            });
    }

    public List<ChatRoom> getUserChatRooms(Long userId) {
        return chatRoomRepository.findByMentorIdOrStudentId(userId, userId);
    }

    @Transactional
    public List<ChatMessage> getMessagesAndMarkRead(Long roomId, Long currentUserId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
        boolean anyMarked = false;

        for (ChatMessage m : messages) {
            if (m.getSender() != null && !m.getSender().getId().equals(currentUserId)) {
                if (m.getIsRead() == null || !m.getIsRead()) {
                    m.setIsRead(true);
                    anyMarked = true;
                }
            }
        }

        if (anyMarked) {
            chatMessageRepository.saveAll(messages);
            try {
                Map<String, Object> readEvent = new HashMap<>();
                readEvent.put("type", "MESSAGES_READ");
                readEvent.put("roomId", roomId);
                readEvent.put("readBy", currentUserId);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, readEvent);
            } catch (Exception ignored) {}
        }

        return messages;
    }

    public List<ChatMessage> getMessages(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    @Transactional
    public ChatMessage sendMessage(Long roomId, User sender, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat Room #" + roomId + " not found."));

        ChatMessage message = new ChatMessage(room, sender, content);
        message.setIsRead(false);
        ChatMessage saved = chatMessageRepository.save(message);

        // Broadcast to live WebSocket topic
        messagingTemplate.convertAndSend("/topic/room/" + roomId, saved);

        return saved;
    }
}