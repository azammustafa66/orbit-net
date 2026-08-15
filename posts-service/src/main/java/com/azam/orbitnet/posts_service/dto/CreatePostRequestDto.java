package com.azam.orbitnet.posts_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequestDto {

    @NotBlank(message = "Post content must not be blank")
    @Size(max = 5000, message = "Post content must be at most 5000 characters")
    private String content;
}
