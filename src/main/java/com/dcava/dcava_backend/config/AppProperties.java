package com.dcava.dcava_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<String> frontendUrls;
    private R2 r2;
    private Firebase firebase;

    // Getters y Setters existentes
    public List<String> getFrontendUrls() {
        return frontendUrls;
    }

    public void setFrontendUrls(List<String> frontendUrls) {
        this.frontendUrls = frontendUrls;
    }

    public R2 getR2() {
        return r2;
    }

    public void setR2(R2 r2) {
        this.r2 = r2;
    }

    public Firebase getFirebase() {
        return firebase;
    }

    public void setFirebase(Firebase firebase) {
        this.firebase = firebase;
    }

    // ---------- Inner class for R2 (existente) ----------
    public static class R2 {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String publicUrl;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }

        public String getPublicUrl() { return publicUrl; }
        public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
    }

    // ---------- UPDATED: Inner class for Firebase ----------
    public static class Firebase {
        private String credentialsJson;  // Cambiado de credentialsPath a credentialsJson

        public String getCredentialsJson() {
            return credentialsJson;
        }

        public void setCredentialsJson(String credentialsJson) {
            this.credentialsJson = credentialsJson;
        }
    }
}

