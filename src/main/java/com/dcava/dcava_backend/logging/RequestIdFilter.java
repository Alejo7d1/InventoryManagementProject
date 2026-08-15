package com.dcava.dcava_backend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Cross-cutting filter that runs BEFORE the Spring Security chain:
 *
 * <ul>
 *   <li>Assigns/accepts a requestId (header {@code X-Request-Id}) for correlation.</li>
 *   <li>Exposes it in the MDC so all request logs include it.</li>
 *   <li>Stores client IP, method and path in the MDC.</li>
 *   <li>Access log: INFO (2xx/3xx), WARN (4xx and slow requests), ERROR (5xx).</li>
 *   <li>Unhandled errors: logged with full stacktrace and requestId,
 *       then re-thrown to keep Spring's default error handling.</li>
 * </ul>
 */
public class RequestIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_UID = "uid";
    public static final String MDC_CLIENT_IP = "clientIp";
    public static final String MDC_METHOD = "method";
    public static final String MDC_PATH = "path";

    /** Threshold (ms) after which a request is considered slow and logged at WARN. */
    private static final int SLOW_REQUEST_THRESHOLD_MS = 2000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank() || requestId.length() > 100) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_CLIENT_IP, resolveClientIp(request));
        MDC.put(MDC_METHOD, request.getMethod());
        MDC.put(MDC_PATH, request.getRequestURI());
        response.setHeader(REQUEST_ID_HEADER, requestId);

        boolean completed = false;
        try {
            filterChain.doFilter(request, response);
            completed = true;
        } catch (Exception e) {
            // Already logged as 5xx; full stacktrace stays here with the requestId.
            log.error("Unhandled exception processing {} {} (status={})",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), e);
            throw e; // keep Spring's default error handling
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            if (completed) {
                logAccess(request, response, durationMs);
            }
            MDC.clear();
        }
    }

    private void logAccess(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        String path = request.getRequestURI();

        // Low-value routes that skip the access log (avoids noise)
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || "/health".equals(path)) {
            return;
        }

        int status = response.getStatus();
        String uid = MDC.get(MDC_UID);
        Object[] args = {
                request.getMethod(),
                path,
                status,
                durationMs,
                uid != null ? uid : "-",
                MDC.get(MDC_CLIENT_IP)
        };

        if (status >= 500) {
            log.error("Request failed: method={} path={} status={} durationMs={} uid={} ip={}", args);
        } else if (status >= 400) {
            log.warn("Request warning: method={} path={} status={} durationMs={} uid={} ip={}", args);
        } else if (durationMs >= SLOW_REQUEST_THRESHOLD_MS) {
            log.warn("SLOW request: method={} path={} status={} durationMs={} uid={} ip={}", args);
        } else {
            log.info("Request: method={} path={} status={} durationMs={} uid={} ip={}", args);
        }
    }

    /**
     * Resolves the client IP respecting proxies (X-Forwarded-For, X-Real-IP).
     * Only used for logs, not for security decisions.
     */
    public static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
