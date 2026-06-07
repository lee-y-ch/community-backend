package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.*;
import com.community.community.entity.User;
import com.community.community.service.AuthService;
import com.community.community.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // ResponseEntity: spring에서 HTTP 응답을 만들어 반환할 수 있게 해주는 객체
    @PostMapping("/users")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {

        try {
            // UserService 호출해서 서비스 로직에서 요청 body 데이터 활용할 수 있도록 request 넘기기
            userService.register(request);

            // 응답 - Map을 사용해서 응답을 만드려고 했는데 null이 허용이 안 돼서, HashMap을 사용하려 했는데 동일한 응답 형태가
            //       많은 api에서 반복되기 때문에 ApiResponse라는 공통된 응답 형식을 처리하는 클래스를 별도로 생성
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<Void>("register_success", null)
            );
        } catch (IllegalArgumentException e) {
            // 400 응답 코드 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<Void>(e.getMessage(), null)
            );
        } catch (IllegalStateException e) {
            // 409 응답 코드 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ApiResponse<Void>(e.getMessage(), null)
            );
        } catch (Exception execption) {
            // 500 응답 코드 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<Void>("internal_server_error", null)
            );
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(
        @PathVariable int userId,
        // accessToken 쿠키가 없어도 Controller에서 직접 401 응답을 만들기 위해 required = false로 받는다.
            // required = true이면 쿠키 누락 시 Spring이 먼저 400을 반환할 수 있다.
        @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);
            User user = userService.getUser(userId, currentUserId);

            // api 명세에 있는 응답 (password를 제외한)을 맞추기 위해 password가 없는 응답 형태를 DTO로 만들어 반환
            UserResponseDTO userResponseDTO = new UserResponseDTO(
                    user.getUserId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getProfileImageUrl()
            );

            GetUserResponseDTO data = new GetUserResponseDTO(userResponseDTO);

            return ResponseEntity.ok(
                    new ApiResponse<>("get_user_success", data)
            );
        } // 기존에 설정한 에러 상태에 대해 맞춰서 응답 코드 생성
        catch (SecurityException e) {
            if (e.getMessage().equals("forbidden")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(e.getMessage(), null)
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable int userId,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody UserUpdateRequestDTO request
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            GetUserResponseDTO data = userService.updateUser(userId, currentUserId, request);

            return ResponseEntity.ok(
                    new ApiResponse<>("update_user_success", data)
            );

            // nickname 누락, 공백, 길이 초과 등 요청값이 잘못된 경우: 400
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

            // 닉네임이 중복된 경우: 409
        } catch (IllegalStateException e) {
            if ("nickname_already_exists".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        new ApiResponse<>("nickname_already_exists", null)
                );
            }

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

    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<ApiResponse<PasswordUpdateResponseDTO>> updatePassword(
            @PathVariable int userId,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody PasswordUpdateRequestDTO request
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            PasswordUpdateResponseDTO data = userService.updatePassword(
                    userId,
                    currentUserId,
                    request
            );

            return ResponseEntity.ok(
                    new ApiResponse<>("update_password_success", data)
            );

            // 비밀번호 누락, 조건 불만족, 비밀번호 확인 불일치인 경우: 422
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
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

            // 수정하려는 사용자가 존재하지 않는 경우: 404
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

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable int userId,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(accessToken);

            userService.deleteUser(userId, currentUserId);

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

            // 탈퇴하려는 사용자가 존재하지 않는 경우: 404
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

    @GetMapping("/users/nickname/check")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        try {
            userService.checkNickname(nickname);

            return ResponseEntity.ok(
                    new ApiResponse<>("nickname_available", null)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ApiResponse<>(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/users/email/check")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            userService.checkEmail(email);

            return ResponseEntity.ok(
                    new ApiResponse<>("email_available", null)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ApiResponse<>(e.getMessage(), null)
            );
        }
    }


}
