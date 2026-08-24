package com.orbitet.services;

import com.orbitet.entities.Post;
import com.orbitet.entities.PostLike;
import com.orbitet.events.PostLiked;
import com.orbitet.exceptions.BadRequestException;
import com.orbitet.exceptions.ResourceNotFoundException;
import com.orbitet.repos.PostLikeRepo;
import com.orbitet.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepo postLikeRepo;
    private final PostRepo postRepo;
    private final KafkaTemplate<Long, PostLiked> postLikeKafkaTemplate;

    @Transactional
    public void likePost(Long postId, Long userId) {
        log.info("User with id {} liking post with id {}", userId, postId);

        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found"));

        boolean hasAlreadyLiked = postLikeRepo.existsByUserIdAndPostId(userId, postId);
        if (hasAlreadyLiked) {
            throw new BadRequestException("Post with id " + postId + " is already liked");
        }

        PostLike postLike = new PostLike();
        postLike.setUserId(userId);
        postLike.setPostId(postId);
        postLikeRepo.saveAndFlush(postLike);

        PostLiked postLiked = PostLiked.builder()
                .postId(postId)
                .ownerUserId(post.getUserId())
                .likedByUserId(userId)
                .build();

        // Keyed by recipient so every notification for one user lands on the same
        // partition, and so stays ordered.
        postLikeKafkaTemplate.send("post_liked_topic", post.getUserId(), postLiked);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
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
