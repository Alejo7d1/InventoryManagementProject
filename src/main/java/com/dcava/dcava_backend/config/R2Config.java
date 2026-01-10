package com.dcava.dcava_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


import java.net.URI;

@Configuration
public class R2Config {

    @Bean
    public S3Client r2Client(AppProperties properties) {
        AppProperties.R2 r2 = properties.getR2();

        return S3Client.builder()
                .endpointOverride(URI.create(r2.getEndpoint()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        r2.getAccessKey(),
                                        r2.getSecretKey()
                                )
                        )
                )
                .region(Region.US_EAST_1)
                // ESTA LÍNEA ES CRÍTICA PARA R2
                .serviceConfiguration(b -> b.chunkedEncodingEnabled(false))
                .build();
    }
}


