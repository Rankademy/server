package maruhxn.rankademy.application.group;

import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRecruitmentPost;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("GroupQueryService 테스트")
class GroupReaderTest {

    @Autowired
    GroupReader groupReader;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Test
    @DisplayName("ID로 그룹 조회")
    void getGroup() {
        // given
        User leader = userRepository.save(createLeader());
        Group group = groupRepository.save(createGroup(leader));

        // when
        Group findGroup = groupReader.get(group.getId());

        // then
        assertThat(findGroup).isNotNull();
        assertThat(findGroup.getName()).isEqualTo("테스트 그룹");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 그룹 조회 시 예외 발생")
    void getGroupWithNonExistId() {
        // when & then
        assertThatThrownBy(() -> groupReader.get(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("그룹 정보가 존재하지 않습니다. id: 999");
    }

    @Test
    @DisplayName("그룹 모집 공고 조회")
    void getRecruitmentPost() {
        // given
        User leader = userRepository.save(createLeader());
        Group group = groupRepository.save(createGroup(leader));
        group.createGroupRecruitmentPost(createRecruitmentRequest());

        // when
        GroupRecruitmentPost recruitmentPost = groupReader.getRecruitmentPost(group.getId());

        // then
        assertThat(recruitmentPost).isNotNull();
        assertThat(recruitmentPost.getTitle()).isEqualTo("그룹원 모집합니다");
        boolean isRecruiting = recruitmentPost.getRecruitmentStartDate().isBefore(LocalDateTime.now()) && recruitmentPost.getRecruitmentEndDate().isAfter(LocalDateTime.now());
        assertThat(isRecruiting).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 그룹의 모집 공고 조회 시 예외 발생")
    void getRecruitmentPostWithNonExistGroup() {
        // when & then
        assertThatThrownBy(() -> groupReader.getRecruitmentPost(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("그룹 정보가 존재하지 않습니다. id: 999");
    }
}
