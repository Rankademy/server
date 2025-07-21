package maruhxn.rankademy.domain.user.dto;

import maruhxn.rankademy.domain.user.OAuth2Provider;

public record UserOAuth2CreateRequest(
        String email,
        String username,
        OAuth2Provider provider,
        String providerId
) {
}
