package maruhxn.rankademy.adapter.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 토큰 응답")
public record TokenDto(
        @Schema(description = "사용자 이메일", example = "user@rankademy.app") String email,
        @Schema(description = "액세스 토큰") String accessToken,
        @Schema(description = "리프레시 토큰") String refreshToken,
        @Schema(description = "소환사 아이콘 번호") Integer summonerIconNum
) {
}
