package maruhxn.rankademy.adapter.storage;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.storage.dto.IssuePreSignedUrlRequest;
import maruhxn.rankademy.adapter.storage.dto.PresignResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PreSignedUrlIssuer {

    private final S3Presigner storagePresigner;

    @Value("${aws.storage.bucket}")
    private String bucket;

    @Value("${aws.storage.region}")
    private String region;

    @Value("${aws.presign.ttl-seconds:600}")
    private Long ttlSeconds;

    public List<PresignResponse> batchIssuePutUrls(List<IssuePreSignedUrlRequest> requests) {
        return requests.stream().map(this::issuePutUrl).toList();
    }

    public PresignResponse issuePutUrl(IssuePreSignedUrlRequest request) {
        PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(request.key());

        if (request.contentType() != null) {
            putBuilder = putBuilder.contentType(request.contentType());
        }

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(ttlSeconds))
                .putObjectRequest(putBuilder.build())
                .build();

        URL presignedUrl = storagePresigner.presignPutObject(presignReq).url();

        return new PresignResponse(
                presignedUrl,
                buildPublicUrl(request),
                Instant.now().plusSeconds(ttlSeconds)
        );
    }

    private String buildPublicUrl(IssuePreSignedUrlRequest request) {
        String encodedKey = encodeKeyPath(request.key());
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, encodedKey);
    }

    private String encodeKeyPath(String key) {
        return Arrays.stream(key.split("/"))
                .map(seg -> URLEncoder
                        .encode(seg, StandardCharsets.UTF_8)
                        .replace("+", "%20")
                        .replace("%7E", "~"))
                .collect(Collectors.joining("/"));
    }
}
