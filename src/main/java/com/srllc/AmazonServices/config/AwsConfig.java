package com.srllc.AmazonServices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsConfig {

        @Bean
        public TextractClient textractClient(
                        @Value("${aws.accessKeyId}") String accessKey,
                        @Value("${aws.secretAccessKey}") String secretKey,
                        @Value("${aws.region}") String region) {

                return TextractClient.builder()
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .region(Region.of(region))
                                .build();
        }

        @Bean
        public RekognitionClient rekognitionClient(
                        @Value("${aws.accessKeyId}") String accessKey,
                        @Value("${aws.secretAccessKey}") String secretKey,
                        @Value("${aws.region}") String region) {

                return RekognitionClient.builder()
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .region(Region.of(region))
                                .build();
        }
}