package maruhxn.rankademy.domain.group.dto;

import lombok.Builder;

@Builder
public record CreateRecruitmentPostRequest(
        String title,

        String content,

        String requirements
) {
}
