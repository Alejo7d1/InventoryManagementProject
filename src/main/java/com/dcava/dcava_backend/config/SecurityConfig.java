package com.dcava.dcava_backend.config;

import com.dcava.dcava_backend.security.FirebaseFilter;
import com.dcava.dcava_backend.service.UserAdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserAdminService userService) throws Exception {

        List<String> publicPatterns = List.of(
                "GET:/products/**",
                "GET:/categories/**",
                "GET:/advertisements/**",
                "/uploads/**"
        );

        FirebaseFilter firebaseFilter =
                new FirebaseFilter(publicPatterns, userService);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/products/**",
                                "/categories/**",
                                "/advertisements/**",
                                "/uploads/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}





