package maruhxn.rankademy.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @Email String email,
        @Size(min = 5, max = 20) String username,
        @Size(min = 8, max = 100) String password
) {
}
