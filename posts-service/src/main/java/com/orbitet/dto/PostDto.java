package com.orbitet.dto;

import com.orbitet.entities.Post;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * @param imageUrl the Cloudinary URL of the post's image, or {@code null} for a text-only
 *                 post — the image part of a create request is optional
 */
@Builder
public record PostDto(Long postId, Long userId, String content, String imageUrl,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static PostDto from(Post post) {
        return PostDto.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
