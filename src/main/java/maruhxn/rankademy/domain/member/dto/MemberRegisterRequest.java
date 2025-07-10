package maruhxn.rankademy.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record MemberRegisterRequest(
        @Email String email,
        @Size(min = 5, max = 20) String username,
        @Size(min = 8, max = 100) String password
) {
}
