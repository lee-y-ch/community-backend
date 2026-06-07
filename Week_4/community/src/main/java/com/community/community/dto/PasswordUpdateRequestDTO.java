package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordUpdateRequestDTO {

    private String password;

    @JsonProperty("password_confirm")
    private String passwordConfirm;
}
