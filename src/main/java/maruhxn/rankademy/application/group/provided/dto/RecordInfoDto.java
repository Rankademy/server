package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승패 기록 정보")
public record RecordInfoDto(
        @Schema(description = "승리 횟수", example = "10") int winCount,
        @Schema(description = "패배 횟수", example = "5") int lossCount,
        @Schema(description = "승률", example = "0.6667") double winRate
) {
    public RecordInfoDto(int winCount, int lossCount) {
        this(winCount, lossCount, (double) winCount / (winCount + lossCount) * 100);
    }
}
