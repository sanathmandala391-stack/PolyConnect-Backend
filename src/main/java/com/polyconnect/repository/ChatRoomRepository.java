package com.polyconnect.repository;

import com.polyconnect.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByMentorIdOrStudentId(Long mentorId, Long studentId);
    Optional<ChatRoom> findByMentorIdAndStudentId(Long mentorId, Long studentId);
}
