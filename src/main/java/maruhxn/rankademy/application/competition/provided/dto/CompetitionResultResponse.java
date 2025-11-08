package maruhxn.rankademy.application.competition.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;

import java.util.List;

@Schema(description = "대항전 결과 응답")
public record CompetitionResultResponse(
        @Schema(description = "대항전 ID", example = "1") Long competitionId,
        @Schema(description = "팀1 정보") TeamInfoResponse team1,
        @Schema(description = "팀2 정보") TeamInfoResponse team2,
        @Schema(description = "세트별 결과") List<SetResultResponse> setResults,
        @Schema(description = "최종 승자 팀 ID", example = "1") Long finalWinnerTeamId
) {
    @Schema(name = "CompetitionResultTeamInfoResponse", description = "결과용 팀 정보")
    public record TeamInfoResponse(
            @Schema(description = "팀 ID", example = "1") Long teamId,
            @Schema(description = "팀 이름") String teamName,
            @Schema(description = "그룹 이름") String groupName,
            @Schema(description = "팀 멤버 목록") List<TeamMemberResponse> teamMembers
    ) {

        @Schema(name = "CompetitionResultTeamMemberResponse", description = "결과용 팀 멤버 정보")
        public record TeamMemberResponse(
                @Schema(description = "멤버 ID", example = "10") Long memberId,
                @Schema(description = "포지션", implementation = LolPosition.class) LolPosition position,
                @Schema(description = "소환사 이름") String summonerName,
                @Schema(description = "소환사 태그") String summonerTag
        ) {
        }
    }

    @Schema(description = "세트 결과")
    public record SetResultResponse(
            @Schema(description = "세트 번호", example = "1") int setNumber,
            @Schema(description = "승자 팀 ID", example = "1") Long winnerTeamId
    ) {
    }
}
