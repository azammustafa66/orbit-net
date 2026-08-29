package com.orbitet.controllers;

import com.orbitet.auth.AuthContextHolder;
import com.orbitet.entities.PostLike;
import com.orbitet.services.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/posts/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}")
    public ResponseEntity<Object> likePost(@PathVariable Long postId) {
        postLikeService.likePost(postId, AuthContextHolder.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Object> deletePostLike(@PathVariable Long postId) {
        postLikeService.unlikePost(postId, AuthContextHolder.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
