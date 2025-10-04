package maruhxn.rankademy.adapter.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record IssuePreSignedUrlRequest(
        @NotBlank
        String key,

        String contentType
) {
}
