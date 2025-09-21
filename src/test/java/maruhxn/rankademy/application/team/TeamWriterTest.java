package maruhxn.rankademy.application.team;


import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("TeamWriter 테스트")
class TeamWriterTest {

    @Autowired
    TeamWriter teamWriter;

    @Autowired
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("팀 생성 성공")
    void create() {
        // given
        User representative = UserFixture.createUser();
        userRepository.save(representative);

        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        for (int i = 0; i < 4; i++) {
            User memberUser = GroupFixture.createMember("member" + i + "@test.com", "member" + i);
            userRepository.save(memberUser);
            members.add(new TeamMember(memberUser, LolPosition.values()[i + 1]));
        }
        TeamCreateRequest teamCreateRequest = TeamFixture.createTeamCreateRequest(representative.getId(), members);

        // when
        Team team = teamWriter.create(teamCreateRequest);
        em.flush();
        em.clear();

        // then
        assertThat(team).isNotNull();
        assertThat(team.getName()).isNotNull();
        assertThat(team.getIntro()).isNotNull();
        assertThat(team.getRepresentativeId()).isEqualTo(representative.getId());
        assertThat(team.getTeamMembers()).hasSize(5);
    }
}