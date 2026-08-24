package com.orbitet.services;

import com.orbitet.auth.AuthContextHolder;
import com.orbitet.client.ConnectionServiceClient;
import com.orbitet.dto.CreatePostRequestDto;
import com.orbitet.dto.PagedResponse;
import com.orbitet.dto.PersonDto;
import com.orbitet.dto.PostDto;
import com.orbitet.entities.Post;
import com.orbitet.events.PostCreated;
import com.orbitet.exceptions.ResourceNotFoundException;
import com.orbitet.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepo postRepo;
    private final ModelMapper modelMapper;
    private final ConnectionServiceClient connectionServiceClient;
    private final KafkaTemplate<Long, PostCreated> postCreatedTemplate;

    private static final int SNEAK_PEEK_LENGTH = 120;

    @Transactional
    public PostDto createPost(CreatePostRequestDto postCreateReq, Long userId) {
        log.info("Creating a post for user id {}", userId);
        Post post = modelMapper.map(postCreateReq, Post.class);
        post.setUserId(userId);
        post = postRepo.saveAndFlush(post);

        List<PersonDto> personDtoList = connectionServiceClient.getFirstDegreeConnections(userId);

        String sneakPeek = sneakPeek(post.getContent());
        for (var person : personDtoList) { // send notification to each connection
            PostCreated postCreated = PostCreated.builder()
                    .postId(post.getId())
                    .userId(person.getUserId())
                    .createdByUserId(userId)
                    .contentSneakPeek(sneakPeek)
                    .build();
            // Keyed by recipient so every notification for one user lands on the same
            // partition, and so stays ordered.
            postCreatedTemplate.send("post_created_topic", person.getUserId(), postCreated);
        }

        return PostDto.from(post);
    }

    @Transactional(readOnly = true)
    public PostDto getPostById(Long postId) {
        log.info("Getting post with id {}", postId);

        List<PersonDto> personDtoList = connectionServiceClient.getFirstDegreeConnections(AuthContextHolder.getCurrentUserId());

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

    /** Keeps the event payload small — subscribers only show a preview of the post. */
    private static String sneakPeek(String content) {
        return content.length() <= SNEAK_PEEK_LENGTH
                ? content
                : content.substring(0, SNEAK_PEEK_LENGTH) + "\u2026";
    }
}
