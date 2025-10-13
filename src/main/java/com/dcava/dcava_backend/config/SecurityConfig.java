package com.dcava.dcava_backend.config;

import com.dcava.dcava_backend.security.FirebaseFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> publicPatterns = List.of(
                //Public endpoints
                "GET:/products/*",          
                "GET:/products",           
                "GET:/products/**",         
                "GET:/categories",
                "GET:/categories/*/products",
                "GET:/products/*/images",
                "GET:/products/deleted",
                //upload resources (public)
                "/uploads/**"
        );

        FirebaseFilter firebaseFilter = new FirebaseFilter(publicPatterns);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) 
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/products/**", "/categories/**", "/advertisements/**", "/uploads/**").permitAll()
                        .anyRequest().authenticated()
                )
                //public routes filter
                .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}




