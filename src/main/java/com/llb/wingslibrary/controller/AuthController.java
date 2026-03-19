package com.llb.wingslibrary.controller;

import com.llb.wingslibrary.config.JwtUtil;
import com.llb.wingslibrary.dto.AuthRequest;
import com.llb.wingslibrary.dto.AuthResponse;
import com.llb.wingslibrary.dto.RefreshRequest;
import com.llb.wingslibrary.service.AuthService;
import com.llb.wingslibrary.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String accessToken =
                jwtUtil.generateAccessToken(userDetails);

        String refreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        refreshTokenService.createRefreshToken(
                userDetails.getUsername(),
                refreshToken
        );

        return ResponseEntity.ok(
                new AuthResponse(accessToken, refreshToken)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshRequest request) {

        // 1️⃣ Validate existing refresh token
        var storedToken =
                refreshTokenService.validateRefreshToken(
                        request.getRefreshToken());

        UserDetails userDetails =
                authService.loadUserByUsername(
                        storedToken.getUsername());

        // 2️⃣ Generate NEW access token
        String newAccessToken =
                jwtUtil.generateAccessToken(userDetails);

        // 3️⃣ Generate NEW refresh token (Rotation)
        String newRefreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        // 4️⃣ Delete old & save new
        refreshTokenService.rotateRefreshToken(
                storedToken,
                newRefreshToken
        );

        return ResponseEntity.ok(
                new AuthResponse(newAccessToken, newRefreshToken)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);

        refreshTokenService.deleteByUsername(username);

        return ResponseEntity.ok("Logged out successfully");
    }
}
