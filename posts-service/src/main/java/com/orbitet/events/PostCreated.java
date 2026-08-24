package com.orbitet.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCreated {
    private Long postId;
    private Long userId;
    private Long createdByUserId;
    private String contentSneakPeek;
}
