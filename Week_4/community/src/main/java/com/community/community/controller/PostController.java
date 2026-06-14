package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.*;
import com.community.community.service.AuthService;
import com.community.community.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(
            // accessToken 쿠키가 없어도 Controller에서 직접 401 응답을 만들기 위해 required = false로 받는다.
            // required = true이면 쿠키 누락 시 Spring이 먼저 400을 반환할 수 있다.
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody PostCreateRequestDTO request
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        CreatePostResponseDTO data = postService.createPost(currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_post_success", data));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable int postId,
            @CookieValue(value = "accessToken", required = false) String accessToken

    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        GetPostResponseDTO data = postService.getPost(postId, currentUserId);

        return ResponseEntity.ok(
                new ApiResponse<>("get_post_success", data)
        );
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size
    ) {
        authService.getCurrentUserId(accessToken);

        GetPostsResponseDTO data = postService.getPosts(cursor, size);

        return ResponseEntity.ok(
                new ApiResponse<>("get_posts_success", data)
        );
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable int postId,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody PostUpdateRequestDTO request
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        PostUpdateResponseDTO data = postService.updatePost(postId, currentUserId, request);

        return ResponseEntity.ok(
                new ApiResponse<>("update_post_success", data)
        );
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable int postId,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        postService.deletePost(postId, currentUserId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable int postId,

            // accessToken 쿠키가 없어도 Spring의 기본 400 응답 대신 직접 401 응답을 반환하기 위해 false로 받는다.
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        PostLikeResponseDTO data = postService.toggleLike(
                postId,
                currentUserId
        );

        return ResponseEntity.ok(
                new ApiResponse<>("toggle_post_like_success", data)
        );
    }
}
