package maruhxn.rankademy.application.user;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.OAuth2Provider;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserQueryService implements UserReader {

    private final UserRepository userRepository;

    @Override
    public User get(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id: " + userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(new Email(email));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. email: " + email));
    }

    @Override
    public User getByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("해당 토큰을 가진 회원을 찾을 수 없습니다."));
    }

    @Override
    public Optional<User> findByProviderAndOauthId(OAuth2Provider provider, String oauthId) {
        return userRepository.findByProviderAndOAuthId(provider, oauthId);
    }
}
