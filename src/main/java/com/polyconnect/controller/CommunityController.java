package com.polyconnect.controller;

import com.polyconnect.entity.Community;
import com.polyconnect.entity.CommunityComment;
import com.polyconnect.entity.CommunityPost;
import com.polyconnect.entity.User;
import com.polyconnect.repository.UserRepository;
import com.polyconnect.security.TenantContext;
import com.polyconnect.service.CommunityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;
    private final UserRepository userRepository;

    public CommunityController(CommunityService communityService, UserRepository userRepository) {
        this.communityService = communityService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Community>> getCommunities() {
        User user = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        return ResponseEntity.ok(communityService.getCommunitiesForUser(user));
    }

    @GetMapping("/{communityId}/posts")
    public ResponseEntity<List<CommunityPost>> getPosts(@PathVariable Long communityId) {
        return ResponseEntity.ok(communityService.getPostsByCommunity(communityId));
    }

    @PostMapping("/{communityId}/posts")
    public ResponseEntity<CommunityPost> createPost(
        @PathVariable Long communityId,
        @RequestBody Map<String, String> body
    ) {
        User user = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        String title = body.get("title");
        String content = body.get("content");
        String category = body.get("category");

        return ResponseEntity.ok(communityService.createPost(communityId, title, content, category, user));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommunityComment>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.getCommentsForPost(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommunityComment> addComment(
        @PathVariable Long postId,
        @RequestBody Map<String, String> body
    ) {
        User user = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        return ResponseEntity.ok(communityService.addComment(postId, body.get("content"), user));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<CommunityPost> likePost(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.likePost(postId));
    }
}
