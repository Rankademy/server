package maruhxn.rankademy.domain.user.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserLabelProviderTest {

    UserLabelProvider userLabelProvider;

    @Test
    void getLabels() {
        userLabelProvider = userId -> List.of("DUMMY");

        assertThat(userLabelProvider.getLabels("puuid"))
                .hasSize(1)
                .contains("DUMMY");
    }
}