package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.user.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class UserReaderTest {

    @Autowired
    UserReader userReader;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void findByRefreshToken() {
        User user = createUser();
        user.addRefreshToken("refresh_token1");
        user.addRefreshToken("refresh_token2");
        userRepository.save(user);

        em.flush();
        em.clear();

        assertThat(userReader.findByRefreshToken("refresh_token1")).isEqualTo(user);
        assertThat(userReader.findByRefreshToken("refresh_token2")).isEqualTo(user);
    }
}