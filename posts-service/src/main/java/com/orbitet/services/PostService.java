package com.orbitet.services;

import com.orbitet.dto.CreatePostRequestDto;
import com.orbitet.dto.PagedResponse;
import com.orbitet.dto.PostDto;
import com.orbitet.entities.Post;
import com.orbitet.exceptions.ResourceNotFoundException;
import com.orbitet.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepo postRepo;
    private final ModelMapper modelMapper;

    @Transactional
    public PostDto createPost(CreatePostRequestDto postCreateReq, Long userId) {
        log.info("Creating a post for user id {}", userId);
        Post post = modelMapper.map(postCreateReq, Post.class);
        post.setUserId(userId);
        // flush so Hibernate populates @CreationTimestamp/@UpdateTimestamp before we map
        Post saved = postRepo.saveAndFlush(post);
        return PostDto.from(saved);
    }

    @Transactional(readOnly = true)
    public PostDto getPost(Long postId) {
        log.info("Getting post with id {}", postId);
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + postId));
        return PostDto.from(post);
    }

    /**
     * @param page 1-indexed page number, as exposed by the API
     */
    @Transactional(readOnly = true)
    public PagedResponse<PostDto> getAllPostsOfUser(Long userId, int page, int pageSize) {
        log.info("Getting posts page {} (size {}) for user id {}", page, pageSize, userId);
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostDto> posts = postRepo.getAllPostsByUserId(userId, pageable).map(PostDto::from);
        return PagedResponse.from(posts);
    }
}
