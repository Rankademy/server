package maruhxn.rankademy.application.group.provided.dto;

import java.time.LocalDateTime;

public record RecruitmentPostResponse(
        Long postId,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
