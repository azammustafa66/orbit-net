package com.azam.orbitnet.posts_service.dto;

import com.azam.orbitnet.posts_service.entities.Post;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostDto(Long postId, Long userId, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static PostDto from(Post post) {
        return PostDto.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
