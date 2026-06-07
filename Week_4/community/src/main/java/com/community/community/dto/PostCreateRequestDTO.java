package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostCreateRequestDTO {

    private String title;
    private String content;

    @JsonProperty("image_url")
    private String imageUrl;
}
