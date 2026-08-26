package com.polyconnect.repository;

import com.polyconnect.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    List<CommunityPost> findByCommunityIdOrderByPinnedDescCreatedAtDesc(Long communityId);
    List<CommunityPost> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
