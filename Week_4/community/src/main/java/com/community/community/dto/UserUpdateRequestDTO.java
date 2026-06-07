package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserUpdateRequestDTO {

    private String nickname;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}