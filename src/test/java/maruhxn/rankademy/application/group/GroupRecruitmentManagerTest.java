package maruhxn.rankademy.application.group;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.provided.GroupRecruitmentManager;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.group.GroupFixture.createGroup;
import static maruhxn.rankademy.domain.group.GroupFixture.createLeader;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("GroupRecruitmentManager 테스트")
class GroupRecruitmentManagerTest {

    @Autowired
    GroupRecruitmentManager groupRecruitmentManager;

    @Autowired
    EntityManager em;

    User leader;

    @BeforeEach
    void setUp() {
        leader = createLeader();
        em.persist(leader);
    }

    @Test
    void startAndCloseRecruitment() {
        Group group = generateGroup();
        group.closeRecruitment();

        assertThat(group.isRecruiting()).isFalse();

        group = groupRecruitmentManager.startRecruitment(group.getId());
        em.flush();
        em.clear();

        assertThat(group.isRecruiting()).isTrue();

        group = groupRecruitmentManager.closeRecruitment(group.getId());
        em.flush();
        em.clear();

        assertThat(group.isRecruiting()).isFalse();
    }

    private Group generateGroup() {
        Group group = createGroup(leader);
        em.persist(group);
        em.flush();
        em.clear();
        return group;
    }
}