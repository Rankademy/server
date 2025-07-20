package maruhxn.rankademy.domain.user.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTitleProviderTest {

    UserTitleProvider userTitleProvider;

    @Test
    void getTitles() {
        userTitleProvider = userId -> List.of("DUMMY");

        assertThat(userTitleProvider.getTitles(1L))
                .hasSize(1)
                .contains("DUMMY");
    }
}