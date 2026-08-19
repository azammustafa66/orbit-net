package com.orbitet.controllers;

import com.orbitet.dto.CreatePostRequestDto;
import com.orbitet.dto.PagedResponse;
import com.orbitet.dto.PostDto;
import com.orbitet.services.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/posts")
public class PostController {

    private final PostService postService;

    // TODO: the gateway will supply X-User-Id once JWT auth is in place; the default is for local testing only
    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestHeader(name = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody CreatePostRequestDto postCreateReq) {
        PostDto postDto = postService.createPost(postCreateReq, userId);
        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<PagedResponse<PostDto>> getAllPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(50) int pageSize) {
        PagedResponse<PostDto> posts = postService.getAllPostsOfUser(userId, page, pageSize);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long postId) {
        PostDto post = postService.getPost(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }
}
