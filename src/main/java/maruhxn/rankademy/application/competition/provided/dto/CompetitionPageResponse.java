package maruhxn.rankademy.application.competition.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.competition.CompetitionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "대항전 목록 페이지 응답")
public record CompetitionPageResponse(
        @Schema(description = "총 대항전 수", example = "5") Long totalCount,
        @Schema(description = "대항전 목록") List<CompetitionListItemResponse> competitions
) {
    @Schema(description = "대항전 목록 아이템")
    public record CompetitionListItemResponse(
            @Schema(description = "대항전 ID", example = "1") Long competitionId,
            @Schema(description = "상대 팀 대학교명") String otherTeamUnivName,
            @Schema(description = "대항전 상태", implementation = CompetitionStatus.class) CompetitionStatus status,
            @Schema(description = "내 팀 정보") TeamInfoResponse myTeam,
            @Schema(description = "상대 팀 정보") TeamInfoResponse otherTeam,
            @Schema(description = "결과 제출 일시") LocalDateTime submittedAt,
            @Schema(description = "승리 여부") boolean isWin,
            @Schema(description = "세트별 결과") List<SetResultResponse> setResults
    ) {
    }
}
