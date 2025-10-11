package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "그룹 가입 요청 응답")
public record JoinRequestResponse(
        @Schema(description = "요청자 ID", example = "10") Long userId,
        @Schema(description = "소환사 이름", example = "Ranker") String summonerName,
        @Schema(description = "소환사 태그", example = "KR1") String summonerTag,
        @Schema(description = "요청 일시") LocalDateTime requestedAt
) {
}
