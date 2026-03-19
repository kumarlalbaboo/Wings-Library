package com.llb.wingslibrary.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

}