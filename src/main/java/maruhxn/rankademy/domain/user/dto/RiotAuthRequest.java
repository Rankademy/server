package maruhxn.rankademy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "라이엇 인증 요청")
public record RiotAuthRequest(
        @NotEmpty
        @Schema(description = "소환사 이름", example = "Ranker") String summonerName,

        @NotEmpty
        @Schema(description = "소환사 태그", example = "KR1") String summonerTag
) {

}
