package com.dcava.dcava_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${app.firebase.credentials-json:}")
    private String credentialsJson;

    @PostConstruct
    public void initFirebase() {
        try {
            GoogleCredentials credentials;

            if (credentialsPath != null && !credentialsPath.isBlank()) {
                try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }
            } else if (credentialsJson != null && !credentialsJson.isBlank()) {
                try (InputStream is = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(is);
                }
            } else {
                throw new IllegalStateException("Firebase credentials not provided. Set FIREBASE_CREDENTIALS_PATH or FIREBASE_CREDENTIALS_JSON.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }
}

