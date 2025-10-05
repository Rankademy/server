package maruhxn.rankademy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import maruhxn.rankademy.domain.user.LolPosition;

@Schema(description = "프로필 수정 요청")
public record ProfileUpdateRequest(
        @Size(min = 2, max = 20)
        @Schema(description = "사용자명", example = "ranker") String username,

        @Size(max = 255)
        @Schema(description = "자기소개") String description,

        @NotNull
        @Schema(description = "주 포지션", implementation = LolPosition.class) LolPosition mainPosition,

        @NotNull
        @Schema(description = "부 포지션", implementation = LolPosition.class) LolPosition subPosition
) {
}
