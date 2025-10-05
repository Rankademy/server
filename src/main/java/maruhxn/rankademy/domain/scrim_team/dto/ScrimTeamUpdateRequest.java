package maruhxn.rankademy.domain.scrim_team.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스크림 팀 수정 요청")
public record ScrimTeamUpdateRequest(
        @Schema(description = "스크림 팀 이름", example = "Rank Scrim") String name,
        @Schema(description = "스크림 팀 소개") String intro
) {
}
