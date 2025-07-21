package maruhxn.rankademy.adapter.security.dto;

public record TokenDto(
        String email,
        String accessToken,
        String refreshToken
) {
}
