package com.azam.orbitnet.posts_service.services;

import com.azam.orbitnet.posts_service.entities.PostLike;
import com.azam.orbitnet.posts_service.exceptions.BadRequestException;
import com.azam.orbitnet.posts_service.exceptions.ResourceNotFoundException;
import com.azam.orbitnet.posts_service.repos.PostLikeRepo;
import com.azam.orbitnet.posts_service.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepo postLikeRepo;
    private final PostRepo postRepo;
    private final ModelMapper modelMapper;

    @Transactional
    public void likePost(Long postId) {
        Long userId = 1L;
        log.info("User with id {} liking post with id {}", userId, postId);

        if (!postRepo.existsById(postId)) {
            throw new ResourceNotFoundException("Post with id " + postId + " does not exist");
        }
        boolean hasAlreadyLiked = postLikeRepo.existsByUserIdAndPostId(userId, postId);
        if (hasAlreadyLiked) {
            throw new BadRequestException("Post with id " + postId + " is already liked");
        }
        PostLike postLike = new PostLike();
        postLike.setUserId(userId);
        postLike.setPostId(postId);
        postLikeRepo.saveAndFlush(postLike);

        // TODO : Send notifications to user when someone likes a post using kafka
    }

    @Transactional
    public void unlikePost(Long postId) {
        Long userId = 1L;
        log.info("User with id {} unliking post with id {}", userId, postId);

        if (!postRepo.existsById(postId)) {
            throw new ResourceNotFoundException("Post with id " + postId + " does not exist");
        }
        boolean hasAlreadyLiked = postLikeRepo.existsByUserIdAndPostId(userId, postId);
        if (!hasAlreadyLiked) {
            throw new BadRequestException("You cannot unlike a post which you've not liked yet");
        }

        postLikeRepo.deleteByPostIdAndUserId(postId, userId);
    }
}
