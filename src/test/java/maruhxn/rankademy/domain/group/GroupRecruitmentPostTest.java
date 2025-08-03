package maruhxn.rankademy.domain.group;

import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GroupRecruitmentPost")
class GroupRecruitmentPostTest {

    private GroupRecruitmentPost post;

    @BeforeEach
    void setUp() {
        User leader = createLeader();
        Group group = createGroup(leader);
        group.upsertRecruitmentPost(createRecruitmentRequest());
        post = group.getRecruitmentPost();
    }

    @Test
    @DisplayName("게시글을 끌어올린다")
    void up() {
        // given
        LocalDateTime beforeUp = LocalDateTime.now();
        post.up(beforeUp);
        LocalDateTime uppedAt = post.getLastUppedAt().plusDays(2);

        // when
        post.up(uppedAt);

        // then
        assertThat(post.getLastUppedAt()).isAfter(beforeUp);
    }

    @Test
    @DisplayName("게시글을 끌어올리기는 하루에 한 번만 가능하다")
    void up_Fail() {
        // given
        LocalDateTime now = LocalDateTime.now();
        post.up(now);
        LocalDateTime uppedAt = now.plusHours(23).plusMinutes(59);

        // when
        assertThatThrownBy(() -> post.up(uppedAt))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activateAndDeactivate() {
        post.deactivate();

        assertThat(post.isActive()).isFalse();

        post.activate();

        assertThat(post.isActive()).isTrue();
    }
}
