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

        // Define aquí los endpoints públicos EXACTAMENTE según tu requerimiento
        List<String> publicPatterns = List.of(
                // Productos públicos y búsquedas
                "GET:/products/*",          // GET /products/{id}
                "GET:/products",            // GET /products?search=...
                "GET:/products/**",         // cubrir otros GET públicos
                "GET:/categories",
                "GET:/categories/*/products",
                "GET:/products/*/images",   // GET imágenes por producto (público)
                "GET:/advertisements/*",
                "GET:/advertisements",      // GET /advertisements?active=
                "GET:/products/deleted",
                // subir archivos y otros recursos estáticos (public)
                "/uploads/**"
        );

        FirebaseFilter firebaseFilter = new FirebaseFilter(publicPatterns);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // configura CORS si necesitas
                .authorizeHttpRequests(auth -> auth
                        // NOTA: los patterns públicos ya los maneja el filter; aquí se mantiene por claridad
                        .requestMatchers("/products/**", "/categories/**", "/advertisements/**", "/uploads/**").permitAll()
                        // todo lo demás requiere auth
                        .anyRequest().authenticated()
                )
                // Añadir filtro que validará token SOLO para rutas no públicas
                .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}




