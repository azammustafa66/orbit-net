package com.azam.orbitnet.posts_service.repos;


import com.azam.orbitnet.posts_service.entities.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepo extends JpaRepository<PostLike, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
