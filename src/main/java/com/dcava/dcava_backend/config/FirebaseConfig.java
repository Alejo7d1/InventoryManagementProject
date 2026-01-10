package com.dcava.dcava_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource("firebase/serviceAccountKey.json").getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully");
        }
    }
}
