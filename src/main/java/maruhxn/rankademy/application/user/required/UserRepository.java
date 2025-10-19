package maruhxn.rankademy.application.user.required;

import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.OAuth2Provider;
import maruhxn.rankademy.domain.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 회원 정보를 저장하거나 조회한다
 */
public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findById(Long userId);

    @Query("select u from User u join fetch u.summonerInfo si where u.id = :userId")
    Optional<User> findByIdWithSummonerInfo(Long userId);

    Optional<User> findByEmail(Email email);

    Optional<User> findByUsername(String username);

    void delete(User user);

    @Query("select u from User u join fetch u.refreshTokens rt where rt.payload = :refreshToken")
    Optional<User> findByRefreshToken(String refreshToken);

    @Query("select u from User u join fetch u.oauthAccounts oa where oa.provider = :provider and oa.oauthId = :oauthId")
    Optional<User> findByProviderAndOAuthId(OAuth2Provider provider, String oauthId);

    List<User> findByLastLoginAtGreaterThanEqual(LocalDateTime dateTime);
}
