package maruhxn.rankademy.application.group.provided.dto;

import java.time.LocalDateTime;

public record MyGroupResponse(
        Long groupId,
        String groupName,
        String groupLogoImg,
        String about,
        LocalDateTime createdAt
) {
}
