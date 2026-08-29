package com.orbitet.controllers;

import com.orbitet.auth.AuthContextHolder;
import com.orbitet.dto.CreatePostRequestDto;
import com.orbitet.dto.PagedResponse;
import com.orbitet.dto.PostDto;
import com.orbitet.services.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/posts")
public class PostController {

    private final PostService postService;

    /**
     * The post body travels as a JSON part alongside the optional file — the two can't
     * share one multipart request under a plain {@code @RequestBody}.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestPart("post") CreatePostRequestDto postCreateReq,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        PostDto postDto = postService.createPost(postCreateReq, AuthContextHolder.requireCurrentUserId(), file);
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
        PostDto post = postService.getPostById(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }
}
