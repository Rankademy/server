package maruhxn.rankademy.application.user.provided;

import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.domain.user.OAuth2Provider;
import maruhxn.rankademy.domain.user.User;

import java.util.Optional;

public interface UserReader {

    User get(Long userId);

    ProfileResponse getProfile(Long userId);

    Optional<User> findByEmail(String email);

    User getByEmail(String email);

    User getByRefreshToken(String refreshToken);

    Optional<User> findByProviderAndOauthId(OAuth2Provider provider, String oauthId);
}
