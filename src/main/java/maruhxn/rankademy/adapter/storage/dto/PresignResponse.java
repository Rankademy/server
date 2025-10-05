package maruhxn.rankademy.adapter.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URL;
import java.time.Instant;

@Schema(description = "Pre-Signed URL 발급 응답")
public record PresignResponse(
        @Schema(description = "업로드용 Pre-Signed URL") URL presignedUrl,
        @Schema(description = "공개 접근 URL") String publicUrl,
        @Schema(description = "URL 만료 시각") Instant expiredAt
) {
}
