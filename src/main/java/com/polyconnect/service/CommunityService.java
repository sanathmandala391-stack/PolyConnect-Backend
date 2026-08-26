package com.polyconnect.service;

import com.polyconnect.entity.*;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.repository.CommunityCommentRepository;
import com.polyconnect.repository.CommunityPostRepository;
import com.polyconnect.repository.CommunityRepository;
import com.polyconnect.repository.ReputationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final ReputationRepository reputationRepository;

    public CommunityService(
        CommunityRepository communityRepository,
        CommunityPostRepository postRepository,
        CommunityCommentRepository commentRepository,
        ReputationRepository reputationRepository
    ) {
        this.communityRepository = communityRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.reputationRepository = reputationRepository;
    }

    public List<Community> getCommunitiesForUser(User user) {
        // Returns statewide + user's college community
        List<Community> communities = communityRepository.findByCommunityType("STATEWIDE");
        if (user.getCollege() != null) {
            communityRepository.findByCollegeIdAndCommunityType(user.getCollege().getId(), "COLLEGE")
                .ifPresent(communities::add);
        }
        return communities;
    }

    public List<CommunityPost> getPostsByCommunity(Long communityId) {
        return postRepository.findByCommunityIdOrderByPinnedDescCreatedAtDesc(communityId);
    }

    @Transactional
    public CommunityPost createPost(Long communityId, String title, String content, String category, User author) {
        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new ResourceNotFoundException("Community #" + communityId + " not found."));

        CommunityPost post = new CommunityPost();
        post.setCommunity(community);
        post.setAuthor(author);
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category != null ? category : "GENERAL");

        CommunityPost saved = postRepository.save(post);

        // Award reputation points for contributing
        reputationRepository.findById(author.getId()).ifPresent(rep -> {
            rep.setPoints(rep.getPoints() + 15);
            rep.setPostsCreated(rep.getPostsCreated() + 1);
            reputationRepository.save(rep);
        });

        return saved;
    }

    @Transactional
    public CommunityComment addComment(Long postId, String content, User author) {
        CommunityPost post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post #" + postId + " not found."));

        CommunityComment comment = new CommunityComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        return commentRepository.save(comment);
    }

    public List<CommunityComment> getCommentsForPost(Long postId) {
        return commentRepository.findByPost_IdOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public CommunityPost likePost(Long postId) {
        CommunityPost post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post #" + postId + " not found."));
        post.setLikesCount(post.getLikesCount() + 1);
        return postRepository.save(post);
    }
}
