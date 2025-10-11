package maruhxn.rankademy.adapter.webapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대학교 랭킹 응답")
public record UnivRankingResponse(
        @Schema(description = "대학교 이름", example = "서울과학기술대학교") String univName,
        @Schema(description = "소속 사용자 수", example = "120") Long totalUserCnt,
        @Schema(description = "총 대항전 수", example = "30") Long competitionTotalCnt,
        @Schema(description = "총 승리 수", example = "18") Long competitionWinCnt,
        @Schema(description = "대표 랭커 정보") RankerDto rankerDto
) {
}
