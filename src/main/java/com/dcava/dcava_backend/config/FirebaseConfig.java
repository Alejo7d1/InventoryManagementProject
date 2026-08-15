package com.dcava.dcava_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${app.firebase.credentials-json:}")
    private String credentialsJson;

    @Value("${app.firebase.type:service_account}")
    private String type;

    @Value("${app.firebase.project-id:}")
    private String projectId;

    @Value("${app.firebase.private-key-id:}")
    private String privateKeyId;

    @Value("${app.firebase.private-key:}")
    private String privateKey;

    @Value("${app.firebase.client-email:}")
    private String clientEmail;

    @Value("${app.firebase.client-id:}")
    private String clientId;

    @Value("${app.firebase.auth-uri:https://accounts.google.com/o/oauth2/auth}")
    private String authUri;

    @Value("${app.firebase.token-uri:https://oauth2.googleapis.com/token}")
    private String tokenUri;

    @Value("${app.firebase.auth-provider-cert-url:https://www.googleapis.com/oauth2/v1/certs}")
    private String authProviderCertUrl;

    @Value("${app.firebase.universe-domain:googleapis.com}")
    private String universeDomain;

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
            } else if (projectId != null && !projectId.isBlank() && privateKey != null && !privateKey.isBlank()) {
                String jsonConstructed = buildServiceAccountJson();
                try (InputStream is = new ByteArrayInputStream(jsonConstructed.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(is);
                }
            } else {
                throw new IllegalStateException("Firebase credentials not provided. Configure .env variables or set credentials path.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            // Solo se loguea el projectId (no es secreto); NUNCA se loguea la private key
            log.info("Firebase initialized: projectId={} clientEmail={}",
                    projectId != null ? projectId : "unknown",
                    clientEmail != null ? clientEmail : "unknown");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase (projectId={})", projectId, e);
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }

    private String buildServiceAccountJson() throws Exception {
        Map<String, String> jsonMap = new LinkedHashMap<>();

        String cleanPrivateKey = privateKey.trim();
        if (cleanPrivateKey.startsWith("\"") && cleanPrivateKey.endsWith("\"")) {
            cleanPrivateKey = cleanPrivateKey.substring(1, cleanPrivateKey.length() - 1);
        }

        cleanPrivateKey = cleanPrivateKey.replace("\\n", "\n");

        jsonMap.put("type", type);
        jsonMap.put("project_id", projectId);
        jsonMap.put("private_key_id", privateKeyId);
        jsonMap.put("private_key", cleanPrivateKey);
        jsonMap.put("client_email", clientEmail);
        jsonMap.put("client_id", clientId);
        jsonMap.put("auth_uri", authUri);
        jsonMap.put("token_uri", tokenUri);
        jsonMap.put("auth_provider_x509_cert_url", authProviderCertUrl);
        jsonMap.put("client_x509_cert_url", "https://www.googleapis.com/robot/v1/metadata/x509/"
                + URLEncoder.encode(clientEmail, StandardCharsets.UTF_8));
        jsonMap.put("universe_domain", universeDomain);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(jsonMap);
    }
}
