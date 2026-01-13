package com.dcava.dcava_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private final AppProperties appProperties;

    public FirebaseConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void initFirebase() {

        String credentialsJson = appProperties.getFirebase().getCredentialsJson();

        if (!FirebaseApp.getApps().isEmpty()) {
            System.out.println("Firebase already initialize");
            return;
        }

        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new IllegalStateException(
                    "app.firebase.credentials-json is not configured");
        }

        try {
            InputStream credentialsStream = new ByteArrayInputStream(
                    credentialsJson.getBytes(StandardCharsets.UTF_8)
            );

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully from JSON configuration");

        } catch (Exception e) {
            System.err.println("ERROR inicializando Firebase desde JSON: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to initialize Firebase from JSON: " + e.getMessage(), e);
        }
    }
}

