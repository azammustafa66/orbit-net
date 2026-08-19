package com.orbitet.repos;

import com.orbitet.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepo extends JpaRepository<Post, Long> {
    Page<Post> getAllPostsByUserId(Long userId, Pageable pageable);
}
