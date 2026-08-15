package com.dcava.dcava_backend.config;

import com.dcava.dcava_backend.logging.RequestIdFilter;
import com.dcava.dcava_backend.security.FirebaseFilter;
import com.dcava.dcava_backend.service.user.UserAdminService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Registra el filtro de requestId/log de acceso ANTES que el chain de
     * Spring Security (order &lt; -100) para que envuelva también las peticiones
     * rechazadas por autenticación.
     */
    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration() {
        FilterRegistrationBean<RequestIdFilter> registration =
                new FilterRegistrationBean<>(new RequestIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserAdminService userService
    ) throws Exception {

        FirebaseFilter firebaseFilter = new FirebaseFilter(userService);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/health",
                                "/products/**",
                                "/categories/**",
                                "/advertisements/**",
                                "/uploads/**",

                                // Swagger
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        firebaseFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
