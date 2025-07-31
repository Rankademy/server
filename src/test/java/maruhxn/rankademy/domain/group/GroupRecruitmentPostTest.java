package maruhxn.rankademy.domain.group;

import maruhxn.rankademy.domain.group.dto.RecruitmentPostUpdateRequest;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GroupRecruitmentPost")
class GroupRecruitmentPostTest {

    private GroupRecruitmentPost post;

    @BeforeEach
    void setUp() {
        User leader = createLeader();
        Group group = createGroup(leader);
        group.createGroupRecruitmentPost(createRecruitmentRequest());
        post = group.getRecruitmentPost();
    }

    @Test
    @DisplayName("게시글 정보를 업데이트한다")
    void update() {
        // given
        String updatedTitle = "수정된 제목";
        String updatedContent = "수정된 내용";
        String updatedRequirements = "수정된 요구사항";
        Integer updatedCapacity = 10;
        LocalDateTime updatedStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime updatedEndDate = LocalDateTime.now().plusDays(10);

        // when
        post.update(new RecruitmentPostUpdateRequest(updatedTitle, updatedContent, updatedRequirements, updatedCapacity, updatedStartDate, updatedEndDate));

        // then
        assertThat(post.getTitle()).isEqualTo(updatedTitle);
        assertThat(post.getContent()).isEqualTo(updatedContent);
        assertThat(post.getRequirements()).isEqualTo(updatedRequirements);
        assertThat(post.getCapacity()).isEqualTo(updatedCapacity);
        assertThat(post.getRecruitmentStartDate()).isEqualTo(updatedStartDate);
        assertThat(post.getRecruitmentEndDate()).isEqualTo(updatedEndDate);
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
}
