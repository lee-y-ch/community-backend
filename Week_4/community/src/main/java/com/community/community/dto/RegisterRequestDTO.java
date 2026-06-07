package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// 회원가입 요청 body를 Java 객체로 받기 위한 DTO
@Getter @Setter
public class RegisterRequestDTO {

    private String email;
    private String password;
    private String nickname;

    // JSON의 profile_image를 Java의 profileImage 필드와 매핑한다.
    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}