package maruhxn.rankademy.application.user.required;

import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * 회원 정보를 저장하거나 조회한다
 */
public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(Email email);

    Optional<User> findByUsername(String username);

    void delete(User user);

    @Query("select u from User u join fetch u.refreshTokens rt where rt.payload = :refreshToken")
    Optional<User> findByRefreshToken(String refreshToken);
}
