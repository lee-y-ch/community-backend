package com.community.community.service;

import com.community.community.dto.*;
import com.community.community.entity.User;
import com.community.community.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입 처리 로직
    public void register(RegisterRequestDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String nickname = request.getNickname();
        String profileImage = request.getProfileImageUrl();

        // 각 에러 처리에 대한 응답 코드 매핑은 UserController에서 진행

        // 필수값 검사 (400)
        if (email == null || email.isBlank()
                || password == null || password.isBlank()
                || nickname == null || nickname.isBlank()
                || profileImage == null || profileImage.isBlank()) {

            // IllegalArgumentException: 잘못된 인자에 대한 에러 처리
            throw new IllegalArgumentException("invalid_register_request");
        }

        // email 중복 검증 (409)
        if (userRepository.existsByEmail(email)) {
            // IllegalStateException: 형식은 올바르나 상태가 올바르지 않은 상황에 대한 에러 처리
            throw new IllegalStateException("email_already_exists");
        }

        // nickname 중복 검증 (409)
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalStateException("nickname_already_exists");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 저장 로직
        User user = new User(email, encodedPassword, nickname, profileImage);
        userRepository.save(user);
    }

    // User를 찾는 메서드
    public User getUser(int pathUserId, int currentUserId) {

        // URL 경로의 userId와 JWT에서 추출한 현재 userId가 일치하는지 확인한다. (403)
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        // findById()는 조회 결과가 없을 수 있기 때문에 Optional<User>를 반환한다.
        // 값이 있으면 User를 그대로 반환하고,
        // 값이 없으면 orElseThrow() 안의 예외를 생성해서 user_not_found로 처리한다.
        return userRepository.findById(pathUserId)
                .orElseThrow(() -> new IllegalStateException("user_not_found"));
    }

    // @Transactional로 설정해서, userRepository.save(user)를 다시 호출하지 않아도 됨.
    // user가 영속 상태이기 때문에 트랜잭션이 끝날 때 JPA가 변경된 값을 감지하고 UPDATE를 실행
    @Transactional
    public GetUserResponseDTO updateUser(int pathUserId, int currentUserId, UserUpdateRequestDTO request) {
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        User user = userRepository.findById(pathUserId)
                .orElseThrow(() -> new IllegalStateException("user_not_found"));

        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new IllegalArgumentException("invalid_update_user_request");
        }

        if (request.getNickname().length() > 10) {
            throw new IllegalArgumentException("invalid_update_user_request");
        }

        if (userRepository.existsByNicknameAndUserIdNot(request.getNickname(), pathUserId)) {
            throw new IllegalStateException("nickname_already_exists");
        }

        user.updateProfile(request.getNickname(), request.getProfileImageUrl());

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );

        return new GetUserResponseDTO(userResponse);
    }

    @Transactional
    public PasswordUpdateResponseDTO updatePassword(
            int pathUserId,
            int currentUserId,
            PasswordUpdateRequestDTO request
    ) {
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        User user = userRepository.findById(pathUserId)
                .orElseThrow(() -> new IllegalStateException("user_not_found"));

        if (request.getPassword() == null || request.getPassword().isBlank()
                || request.getPasswordConfirm() == null || request.getPasswordConfirm().isBlank()) {
            throw new IllegalArgumentException("invalid_update_password_request");
        }

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("invalid_update_password_request");
        }

        if (!isValidPassword(request.getPassword())) {
            throw new IllegalArgumentException("invalid_update_password_request");
        }

        user.updatePassword(passwordEncoder.encode(request.getPassword()));

        return new PasswordUpdateResponseDTO(pathUserId);
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);

            if (Character.isUpperCase(ch)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowerCase = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }

    public void deleteUser(int pathUserId, int currentUserId) {
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        User user = userRepository.findById(pathUserId)
                .orElseThrow(() -> new IllegalStateException("user_not_found"));

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("invalid_nickname_check_request");
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalStateException("duplicate_nickname");
        }
    }

    @Transactional(readOnly = true)
    public void checkEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("invalid_email_check_request");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("duplicate_email");
        }
    }

}
