package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.*;
import com.community.community.service.AuthService;
import com.community.community.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final AuthService authService;
    private final CommentService commentService;

    public CommentController(AuthService authService, CommentService commentService) {
        this.authService = authService;
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable int postId,
            // accessToken 쿠키가 없어도 Controller에서 직접 401 응답을 만들기 위해 required = false로 받는다.
            // required = true이면 쿠키 누락 시 Spring이 먼저 400을 반환할 수 있다.
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody CommentCreateRequestDTO request
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        CommentCreateResponseDTO data = commentService.createComment(
                postId,
                currentUserId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("create_comment_success", data)
        );
    }

    // accessToken 쿠키가 없어도 Controller 내부에서 직접 인증 실패를 처리할 수 있도록
    // required = false로 설정한다. 쿠키가 없으면 accessToken에 null이 전달되고,
    // AuthService에서 이를 확인하여 401 Unauthorized를 반환한다.
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable int postId,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        GetCommentsResponseDTO data = commentService.getComments(
                postId,
                cursor,
                size,
                currentUserId
        );

        return ResponseEntity.ok(
                new ApiResponse<>("get_comments_success", data)
        );
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable int commentId,

            // accessToken 쿠키가 없어도 Spring의 기본 400 응답 대신 직접 401 응답을 반환하기 위해 false로 받는다.
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody CommentUpdateRequestDTO request
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        CommentUpdateResponseDTO data = commentService.updateComment(
                commentId,
                currentUserId,
                request
        );

        return ResponseEntity.ok(
                new ApiResponse<>("update_comment_success", data)
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable int commentId,

            // accessToken 쿠키가 없어도 Spring의 기본 400 응답 대신 직접 401 응답을 반환하기 위해 false로 받는다.
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        int currentUserId = authService.getCurrentUserId(accessToken);

        commentService.deleteComment(commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
