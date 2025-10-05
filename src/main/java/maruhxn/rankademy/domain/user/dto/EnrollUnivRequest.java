package maruhxn.rankademy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Year;

@Schema(description = "대학교 정보 등록 요청")
public record EnrollUnivRequest(
        @NotEmpty
        @Size(max = 100)
        @Schema(description = "대학교 이름", example = "서울과학기술대학교")
        String univName,

        @Email
        @Schema(description = "대학교 이메일", example = "user@seoultech.ac.kr")
        String univMail,

        @NotNull
        @Schema(description = "재학 여부", example = "true")
        Boolean inCollege,

        @NotNull
        @Schema(description = "입학년도", example = "2021")
        int admissionYear,

        @NotEmpty
        @Size(min = 1, max = 100)
        @Schema(description = "전공", example = "컴퓨터공학과")
        String major
) {
    public EnrollUnivRequest {
        int currentYear = Year.now().getValue();
        if (admissionYear > currentYear) {
            throw new IllegalArgumentException("입학년도가 올바르지 않습니다: " + admissionYear);
        }
    }
}
