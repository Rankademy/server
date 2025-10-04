package maruhxn.rankademy.adapter.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfig {

    @Value("${aws.storage.region}")
    private String region;

    @Value("${aws.credentials.accessKey}")
    private String accessKey;

    @Value("${aws.credentials.secretKey}")
    private String secretKey;

    private StaticCredentialsProvider creds() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean
    public S3Client storageClient() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(creds())
                .build();
    }

    @Bean
    public S3Presigner storagePresigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(creds())
                .build();
    }
}
