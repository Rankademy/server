package maruhxn.rankademy.domain.member.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTitleProviderTest {

    MemberTitleProvider memberTitleProvider;

    @Test
    void getTitles() {
        memberTitleProvider = memberId -> List.of("DUMMY");

        assertThat(memberTitleProvider.getTitles(1L))
                .hasSize(1)
                .contains("DUMMY");
    }
}