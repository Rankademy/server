package maruhxn.rankademy.adapter.storage.dto;

import java.net.URL;
import java.time.Instant;

public record PresignResponse(
        URL presignedUrl,
        String publicUrl,
        Instant expiredAt
) {
}
