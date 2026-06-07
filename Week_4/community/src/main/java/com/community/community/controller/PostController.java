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
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            CreatePostResponseDTO data = postService.createPost(currentUserId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("create_post_success", data));

            // 제목/본문 누락, 제목 길이 초과 등 잘못된 게시글 작성 요청에 대한 400 응답
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // accessToken이 없거나 만료/변조되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

        }   // 예상하지 못한 서버 내부 오류에 대한 500 응답
          catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable int postId,
            @CookieValue(value = "accessToken", required = false) String accessToken

    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            GetPostResponseDTO data = postService.getPost(postId, currentUserId);

            return ResponseEntity.ok(
                    new ApiResponse<>("get_post_success", data)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("post_not_found", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("internal_server_error", null));
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size
    ) {
        try {
            authService.getCurrentUserId(accessToken);

            GetPostsResponseDTO data = postService.getPosts(cursor, size);

            return ResponseEntity.ok(
                    new ApiResponse<>("get_posts_success", data)
            );

            // cursor, size 값이 숫자가 아니거나 잘못된 범위인 경우: 400
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // accessToken이 없거나 만료/변조되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable int postId,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody PostUpdateRequestDTO request
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            PostUpdateResponseDTO data = postService.updatePost(postId, currentUserId, request);

            return ResponseEntity.ok(
                    new ApiResponse<>("update_post_success", data)
            );

            // title, content 누락 또는 제목 길이 초과 등 요청값이 잘못된 경우: 400
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // accessToken이 없거나 만료/변조되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            if ("forbidden".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>("forbidden", null)
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 수정하려는 게시글이 존재하지 않는 경우: 404
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable int postId,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            postService.deletePost(postId, currentUserId);

            return ResponseEntity.noContent().build();

            // accessToken이 없거나 만료/변조되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            if ("forbidden".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>("forbidden", null)
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 삭제하려는 게시글이 존재하지 않는 경우: 404
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable int postId,

            // accessToken 쿠키가 없어도 Spring의 기본 400 응답 대신 직접 401 응답을 반환하기 위해 false로 받는다.
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            PostLikeResponseDTO data = postService.toggleLike(
                    postId,
                    currentUserId
            );

            return ResponseEntity.ok(
                    new ApiResponse<>("toggle_post_like_success", data)
            );

            // accessToken이 없거나 만료/변조되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 좋아요를 누르려는 게시글이 존재하지 않는 경우: 404
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }


}
