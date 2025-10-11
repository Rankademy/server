package maruhxn.rankademy.domain.scrim_team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.scrim_team.ScrimTeamMember;

import java.util.Set;

@Schema(description = "스크림 팀 생성 요청")
public record ScrimTeamCreateRequest(
        @Schema(description = "스크림 팀 이름", example = "Rank Scrim") String name,
        @Schema(description = "스크림 팀 소개") String intro,
        @Schema(description = "대표자 사용자 ID", example = "100") Long representativeId,
        @Schema(description = "팀 멤버 목록") Set<ScrimTeamMember> members
) {
}
