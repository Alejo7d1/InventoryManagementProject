package com.dcava.dcava_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Health", description = "Endpoint de salud del servidor")
public class HealthController {

    @Value("${spring.application.name:dcava-backend}")
    private String appName;

    @Operation(summary = "Comprueba la salud del servidor", description = "Devuelve el estado del servidor y la marca de tiempo actual.")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", appName);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
