package maruhxn.rankademy.adapter.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Pre-Signed URL 발급 요청")
public record IssuePreSignedUrlRequest(
        @NotBlank
        @Schema(description = "저장될 파일 키", example = "uploads/sample.png") String key,

        @Schema(description = "파일 콘텐츠 타입", example = "image/png") String contentType
) {
}
