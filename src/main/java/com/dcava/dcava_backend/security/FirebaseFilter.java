package com.dcava.dcava_backend.security;

import com.dcava.dcava_backend.service.UserAdminService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.util.AntPathMatcher;
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

    private final List<String> publicPatterns;
    private final UserAdminService userService;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public FirebaseFilter(List<String> publicPatterns, UserAdminService userService) {
        this.publicPatterns = publicPatterns;
        this.userService = userService;
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Preflight CORS
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        for (String pattern : publicPatterns) {
            if (pattern.contains(":")) {
                String[] parts = pattern.split(":", 2);
                String pMethod = parts[0];
                String pPath = parts[1];

                if (pMethod.equalsIgnoreCase(method) && matcher.match(pPath, path)) {
                    return true;
                }
            } else {
                if (matcher.match(pattern, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Public endpoint → no auth
        if (isPublicPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization token required");
            return;
        }

        try {
            String token = header.substring(7);
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decoded.getUid();

            // 🔐 AUTORIZACIÓN REAL
            if (!userService.existsByUid(uid)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not registered in system");
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(uid, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired Firebase token");
        }
    }
}

