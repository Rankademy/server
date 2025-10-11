package maruhxn.rankademy.domain.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대항전 결과 이의 제기 요청")
public record OpposeResultRequest(
        @Schema(description = "이의 제기 사유", example = "상대 팀 선수 정보가 잘못되었습니다") String reason
) {
}
