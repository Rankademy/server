package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

@Schema(description = "그룹 구성원 요약 정보")
public record GroupMemberResponse(
        @Schema(description = "소환사 이름", example = "Ranker") String summonerName,
        @Schema(description = "소환사 태그", example = "KR1") String summonerTag,
        @Schema(description = "소환사 아이콘 번호", example = "1234") int summonerIconId,
        @Schema(description = "전공", example = "컴퓨터공학과") String major,
        @Schema(description = "입학년도", example = "2021") int admissionYear,
        @Schema(description = "주 포지션", implementation = LolPosition.class) LolPosition mainPosition,
        @Schema(description = "보조 포지션", implementation = LolPosition.class) LolPosition subPosition,
        @Schema(description = "현재 티어 정보") TierInfo tierInfo,
        @Schema(description = "전적 요약") RecordInfoDto recordInfo
) {
}
