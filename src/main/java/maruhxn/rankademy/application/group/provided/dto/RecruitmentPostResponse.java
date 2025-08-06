package maruhxn.rankademy.application.group.provided.dto;

import java.time.LocalDateTime;

public record RecruitmentPostResponse(
        Long postId,
        Long groupId,
        String groupName,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
