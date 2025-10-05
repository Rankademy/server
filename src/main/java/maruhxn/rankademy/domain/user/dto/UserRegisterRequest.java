package maruhxn.rankademy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 가입 요청")
public record UserRegisterRequest(
        @Email @Schema(description = "사용자 이메일", example = "user@rankademy.app") String email,
        @Size(min = 2, max = 20) @Schema(description = "사용자명", example = "ranker") String username,
        @Size(min = 8, max = 100) @Schema(description = "비밀번호") String password
) {
}
