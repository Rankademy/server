package maruhxn.rankademy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import maruhxn.rankademy.domain.user.LolPosition;

import java.time.Year;

@Schema(description = "프로필 수정 요청")
public record ProfileUpdateRequest(
        @Size(min = 2, max = 20)
        @Schema(description = "사용자명", example = "ranker") String username,

        @Schema(description = "자기소개") String description,

        @NotNull
        @Schema(description = "주 포지션", implementation = LolPosition.class) LolPosition mainPosition,

        @NotNull
        @Schema(description = "부 포지션", implementation = LolPosition.class) LolPosition subPosition,

        @NotNull
        @Schema(description = "입학년도", example = "2021")
        Integer admissionYear,

        @NotEmpty
        @Size(min = 1, max = 100)
        @Schema(description = "전공", example = "컴퓨터공학과")
        String major
) {

    public ProfileUpdateRequest {
        int currentYear = Year.now().getValue();
        if (admissionYear > currentYear) {
            throw new IllegalArgumentException("입학년도가 올바르지 않습니다: " + admissionYear);
        }
    }
}
