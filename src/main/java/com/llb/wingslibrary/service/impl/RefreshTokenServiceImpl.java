package com.llb.wingslibrary.service.impl;

import com.llb.wingslibrary.entity.RefreshToken;
import com.llb.wingslibrary.repository.RefreshTokenRepository;
import com.llb.wingslibrary.service.RefreshTokenService;
import io.jsonwebtoken.JwtException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpiration;

    @Override
    public RefreshToken createRefreshToken(String username, String token) {

        repository.deleteByUsername(username); // invalidate old

        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(token)
                .expiryDate(LocalDateTime.now()
                        .plusSeconds(refreshExpiration / 1000))
                .build();

        return repository.save(refreshToken);
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() ->
                        new JwtException("Invalid refresh token"));

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            repository.delete(refreshToken);
            throw new JwtException("Refresh token expired");
        }

        return refreshToken;
    }

    @Override
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String newToken) {

        repository.delete(oldToken); // delete old token immediately

        RefreshToken refreshToken = RefreshToken.builder()
                .username(oldToken.getUsername())
                .token(newToken)
                .expiryDate(LocalDateTime.now()
                        .plusSeconds(refreshExpiration / 1000))
                .build();

        return repository.save(refreshToken);
    }

    @Override
    public void deleteByUsername(String username) {
        repository.deleteByUsername(username);
    }
}