package com.dcava.dcava_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_CREDENTIALS_PATH =
            System.getProperty("user.dir") + "/firebase/serviceAccountKey.json";


    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            File credentialsFile = new File(FIREBASE_CREDENTIALS_PATH);

            if (!credentialsFile.exists()) {
                throw new IllegalStateException(
                        "Firebase credentials file not found: " + FIREBASE_CREDENTIALS_PATH);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsFile)))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully");
        }
    }
}

