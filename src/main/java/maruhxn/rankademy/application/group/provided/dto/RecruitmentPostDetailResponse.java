package maruhxn.rankademy.application.group.provided.dto;

import java.time.LocalDateTime;

public record RecruitmentPostDetailResponse(
        Long postId,
        String title,
        String content,
        String requirements,
        LocalDateTime createdAt,
        boolean isJoined
) {
}
