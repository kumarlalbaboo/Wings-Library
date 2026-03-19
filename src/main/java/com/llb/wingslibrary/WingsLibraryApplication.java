package com.llb.wingslibrary;

import com.llb.wingslibrary.entity.Admin;
import com.llb.wingslibrary.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class WingsLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(WingsLibraryApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(AdminRepository repo, PasswordEncoder encoder) {
        return args -> {

            if (repo.findByUsername("admin").isEmpty()) {

                Admin admin = Admin.builder()
                        .username("admin")
                        .password(encoder.encode("admin123"))
                        .role("ROLE_ADMIN")
                        .build();

                repo.save(admin);
            }
        };
    }

}
