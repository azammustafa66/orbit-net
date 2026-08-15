package com.azam.orbitnet.posts_service.repos;

import com.azam.orbitnet.posts_service.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepo extends JpaRepository<Post, Long> {
    Page<Post> getAllPostsByUserId(Long userId, Pageable pageable);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
