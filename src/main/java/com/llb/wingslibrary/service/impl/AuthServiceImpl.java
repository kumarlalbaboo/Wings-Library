package com.llb.wingslibrary.service.impl;

import com.llb.wingslibrary.entity.Admin;
import com.llb.wingslibrary.repository.AdminRepository;
import com.llb.wingslibrary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Admin not found"));

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .roles("ADMIN")   // NOT ROLE_ADMIN
                .build();
    }
}
