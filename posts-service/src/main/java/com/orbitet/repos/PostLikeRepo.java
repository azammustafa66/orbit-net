package com.orbitet.repos;

import com.orbitet.entities.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepo extends JpaRepository<PostLike, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByPostIdAndUserId(Long postId, Long userId);
}
