package maruhxn.rankademy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "대학교 정보 등록 요청")
public record EnrollUnivRequest(
        @NotEmpty
        @Size(max = 100)
        @Schema(description = "대학교 이름", example = "서울과학기술대학교")
        String univName,

        @Email
        @Schema(description = "대학교 이메일", example = "user@seoultech.ac.kr")
        String univMail

) {
}
