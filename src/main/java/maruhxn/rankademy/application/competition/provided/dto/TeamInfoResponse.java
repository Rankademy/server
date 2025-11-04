package maruhxn.rankademy.application.competition.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;

import java.util.List;

@Schema(description = "대항전 팀 정보")
public record TeamInfoResponse(
        @Schema(description = "팀 ID", example = "1") Long teamId,
        @Schema(description = "팀 이름") String teamName,
        @Schema(description = "그룹 로고") String groupLogo,
        @Schema(description = "그룹 이름") String groupName,
        @Schema(description = "팀 멤버 목록") List<TeamMemberResponse> teamMembers
) {

    @Schema(description = "대항전 팀 멤버 정보")
    public record TeamMemberResponse(
            @Schema(description = "멤버 ID", example = "10") Long memberId,
            @Schema(description = "포지션", implementation = LolPosition.class) LolPosition position,
            @Schema(description = "소환사 이름") String summonerName,
            @Schema(description = "소환사 태그") String summonerTag,
            @Schema(description = "소환사 아이콘") Integer summonerIcon
    ) {
    }
}
