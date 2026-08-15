package com.dcava.dcava_backend.security;

import com.dcava.dcava_backend.logging.RequestIdFilter;
import com.dcava.dcava_backend.service.user.UserAdminService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class FirebaseFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseFilter.class);

    private final UserAdminService userService;

    public FirebaseFilter(UserAdminService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Ignore public endpoints + Swagger
        if (path.startsWith("/health") ||
                path.startsWith("/products") ||
                path.startsWith("/categories") ||
                path.startsWith("/advertisements") ||
                path.startsWith("/uploads") ||

                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")) {

            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
            log.warn("Missing or malformed Authorization header: method={} path={} ip={}",
                    request.getMethod(), request.getRequestURI(), RequestIdFilter.resolveClientIp(request));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization token");
            return;
        }

        try {
            String token = header.substring(7);
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decoded.getUid();

            if (!userService.existsByUid(uid)) {
                log.warn("Authenticated user not registered in system: uid={} path={} ip={}", uid, request.getRequestURI(), RequestIdFilter.resolveClientIp(request));
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not registered in system");
                return;
            }

            // uid en MDC para correlacionar todos los logs de la petición
            MDC.put(RequestIdFilter.MDC_UID, uid);
            log.info("Authenticated: uid={} path={}", uid, request.getRequestURI());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(uid, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (FirebaseAuthException e) {
            log.warn("Invalid or expired Firebase token: method={} path={} ip={} reason={}", request.getMethod(), request.getRequestURI(),
                    RequestIdFilter.resolveClientIp(request), e.getErrorCode());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired Firebase token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
