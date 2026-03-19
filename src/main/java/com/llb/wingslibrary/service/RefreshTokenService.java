package com.llb.wingslibrary.service;

import com.llb.wingslibrary.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String username, String token);

    RefreshToken rotateRefreshToken(RefreshToken oldToken, String newToken);

    RefreshToken validateRefreshToken(String token);

    void deleteByUsername(String username);
}