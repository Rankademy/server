package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static maruhxn.rankademy.domain.user.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class UserReaderTest extends IntegrationTestSupport {

    @Autowired
    UserReader userReader;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void getByRefreshToken() {
        User user = createUser();
        user.addRefreshToken("refresh_token1");
        user.addRefreshToken("refresh_token2");
        userRepository.save(user);

        em.flush();
        em.clear();

        assertThat(userReader.getByRefreshToken("refresh_token1")).isEqualTo(user);
        assertThat(userReader.getByRefreshToken("refresh_token2")).isEqualTo(user);
    }
}