package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.webapi.dto.SearchedUserResponse;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static maruhxn.rankademy.domain.user.UserFixture.createAuthorizedMember;
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

    @Test
    @DisplayName("소환사명과 태그 키워드로 유저 검색")
    void searchUsersWithKeyword() {
        // given
        User alpha = userRepository.save(createAuthorizedMember("alpha@rankademy.app", "alpha"));
        userRepository.save(createAuthorizedMember("beta@rankademy.app", "beta"));

        em.flush();
        em.clear();

        // when
        List<SearchedUserResponse> results = userReader.searchUsers("alpha#KR1");

        // then
        assertThat(results).hasSize(1);
        SearchedUserResponse result = results.get(0);
        assertThat(result.userId()).isEqualTo(alpha.getId());
        assertThat(result.summonerName()).isEqualTo("alpha");
        assertThat(result.summonerTag()).isEqualTo("KR1");
    }

    @Test
    @DisplayName("유저 검색은 최대 4명까지만 반환")
    void searchUsersWithLimit() {
        // given
        for (int i = 0; i < 6; i++) {
            userRepository.save(createAuthorizedMember("searcher" + i + "@rankademy.app", "searcher" + i));
        }

        em.flush();
        em.clear();

        // when
        List<SearchedUserResponse> results = userReader.searchUsers("searcher");

        // then
        assertThat(results).hasSize(4);
        assertThat(results)
                .allMatch(response -> response.summonerName().startsWith("searcher"));
    }
}
