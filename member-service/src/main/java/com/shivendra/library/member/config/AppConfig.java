package com.shivendra.library.member.config;

import com.shivendra.library.member.model.Member;
import com.shivendra.library.member.model.Role;
import com.shivendra.library.member.repository.MemberRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Demo admin so the system can be tried right away. Change or remove for real use. */
    @Bean
    ApplicationRunner seedAdmin(MemberRepository members, PasswordEncoder encoder) {
        return args -> {
            if (!members.existsByEmail("admin@library.com")) {
                members.save(new Member("Library Admin", "admin@library.com",
                        encoder.encode("admin123"), Role.ADMIN));
            }
        };
    }
}
