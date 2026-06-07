package com.community.community.dto;

import lombok.Getter;
import lombok.Setter;

// /auth/login 요청에 대한 DTO
@Getter @Setter
public class LoginRequestDTO {

    private String email;
    private String password;
}
