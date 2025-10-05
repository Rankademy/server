package maruhxn.rankademy.application.competition.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.domain.competition.CompetitionStatus;

@Schema(description = "대항전 상세 응답")
public record CompetitionDetailResponse(
        @Schema(description = "대항전 ID", example = "1") Long competitionId,
        @Schema(description = "대항전 상태", implementation = CompetitionStatus.class) CompetitionStatus status,
        @Schema(description = "첫 번째 팀 정보") TeamDetailResponse team1,
        @Schema(description = "두 번째 팀 정보") TeamDetailResponse team2
) {

}
