package maruhxn.rankademy.application.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

import java.util.List;

@Schema(description = "내 프로필 응답")
public record MyProfileResponse(
        @Schema(description = "사용자 ID", example = "10") Long id,
        @Schema(description = "사용자 이름") String username,
        @Schema(description = "소환사 정보") SummonerInfoResponse summonerInfo,
        @Schema(description = "대학교 정보") UnivInfoResponse univInfo,
        @Schema(description = "자기소개") String description,
        @Schema(description = "자주 플레이한 챔피언 ID 목록") List<String> mostChampionIds,
        @Schema(description = "주 포지션", implementation = LolPosition.class) LolPosition mainPosition,
        @Schema(description = "부 포지션", implementation = LolPosition.class) LolPosition subPosition
) {

    @Schema(description = "대학교 상세 정보")
    public record UnivInfoResponse(
            @Schema(description = "대학교 이름") String univName,
            @Schema(description = "대학교 이메일") String univMail,
            @Schema(description = "전공") String major,
            @Schema(description = "입학년도") Integer admissionYear
    ) {
    }

    @Schema(description = "소환사 정보")
    public record SummonerInfoResponse(
            @Schema(description = "소환사 PUUID") String puuid,
            @Schema(description = "소환사 이름") String summonerName,
            @Schema(description = "소환사 태그") String summonerTag,
            @Schema(description = "소환사 아이콘 번호") int summonerIcon,
            @Schema(description = "현재 티어 정보") TierInfo tierInfo,
            @Schema(description = "승리 횟수", example = "20") int winCount,
            @Schema(description = "패배 횟수", example = "10") int lossCount,
            @Schema(description = "승률(%)", example = "66.7") double winRate
    ) {
        public SummonerInfoResponse(String puuid, String summonerName, String summonerTag, int summonerIcon, TierInfo tierInfo, int winCount, int lossCount) {
            this(
                    puuid,
                    summonerName,
                    summonerTag,
                    summonerIcon,
                    tierInfo,
                    winCount,
                    lossCount,
                    (double) winCount / Math.max(1, winCount + lossCount) * 100.0
            );
        }
    }
}
