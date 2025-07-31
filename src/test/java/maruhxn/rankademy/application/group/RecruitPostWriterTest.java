package maruhxn.rankademy.application.group;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.provided.RecruitPostWriter;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRecruitmentPost;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostCreateRequest;
import maruhxn.rankademy.domain.group.dto.RecruitmentPostUpdateRequest;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("RecruitPostWriter 테스트")
class RecruitPostWriterTest {

    @Autowired
    RecruitPostWriter recruitPostWriter;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    User leader;
    Group group;

    @BeforeEach
    void setUp() {
        leader = createLeader();
        userRepository.save(leader);

        group = createGroup(leader);
        groupRepository.save(group);
    }

    @Test
    @DisplayName("모집 공고 생성")
    void createRecruitPost() {
        // given
        RecruitmentPostCreateRequest request = createRecruitmentRequest();
        em.flush();
        em.clear();

        // when
        GroupRecruitmentPost newPost = recruitPostWriter.createRecruitPost(group.getId(), request);
        em.flush();
        em.clear();

        // then
        assertThat(newPost).isNotNull();
        assertThat(newPost.getTitle()).isEqualTo(request.title());
        assertThat(newPost.getContent()).isEqualTo(request.content());
        assertThat(newPost.getRequirements()).isEqualTo(request.requirements());
        assertThat(newPost.getCapacity()).isEqualTo(request.capacity());
    }

    @Test
    @DisplayName("모집 공고 수정")
    void updateRecruitPost() {
        // given
        RecruitmentPostCreateRequest createRequest = createRecruitmentRequest();
        recruitPostWriter.createRecruitPost(group.getId(), createRequest);
        em.flush();
        em.clear();

        RecruitmentPostUpdateRequest updateRequest = new RecruitmentPostUpdateRequest(
                "수정된 모집 공고",
                "수정된 내용",
                createRequest.requirements(),
                createRequest.capacity(),
                createRequest.recruitmentStartDate(),
                createRequest.recruitmentEndDate()
        );

        // when
        GroupRecruitmentPost updatedPost = recruitPostWriter.updateRecruitPost(group.getId(), updateRequest);
        em.flush();
        em.clear();

        // then
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 모집 공고");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("모집 공고 끌어올리기")
    void upRecruitPost() {
        // given
        RecruitmentPostCreateRequest createRequest = createRecruitmentRequest();
        recruitPostWriter.createRecruitPost(group.getId(), createRequest);
        em.flush();
        em.clear();

        // when
        GroupRecruitmentPost uppedPost = recruitPostWriter.up(group.getId());
        em.flush();
        em.clear();

        // then
        assertThat(uppedPost.getLastUppedAt()).isNotNull();
    }

    @Test
    @DisplayName("모집 공고 끌어올리기 - 24시간 이내 재요청 시 예외 발생")
    void upRecruitPostFail() {
        // given
        RecruitmentPostCreateRequest createRequest = createRecruitmentRequest();
        recruitPostWriter.createRecruitPost(group.getId(), createRequest);
        recruitPostWriter.up(group.getId());
        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> recruitPostWriter.up(group.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("24시간 이내에는 다시 up할 수 없습니다.");
    }
}
