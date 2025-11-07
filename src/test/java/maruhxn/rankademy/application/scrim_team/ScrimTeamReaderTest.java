package maruhxn.rankademy.application.scrim_team;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.scrim_team.provided.ScrimTeamReader;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamPageResponse;
import maruhxn.rankademy.application.scrim_team.required.ScrimTeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.scrim_team.ScrimTeam;
import maruhxn.rankademy.domain.scrim_team.ScrimTeamMember;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.Rank;
import maruhxn.rankademy.domain.user.Tier;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScrimTeamReader 테스트")
class ScrimTeamReaderTest extends IntegrationTestSupport {

    @Autowired
    ScrimTeamReader scrimTeamReader;

    @Autowired
    ScrimTeamRepository scrimTeamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    private User leader;

    @BeforeEach
    void setUp() {
        leader = persistUserWithTier("leader@scrim.app", "scrim-leader", tierInfo(1200));
    }

    @Test
    @DisplayName("리더 스크림 팀이 있을 때 추천 스크림 팀 3개가 상단에 노출된다")
    void getScrimTeamListWithRecommendations() {
        // given
        ScrimTeam leaderTeam = createScrimTeam("leader-team", leader, tierInfo(1200));

        ScrimTeam nearFirst = createOpponentTeam("near-first", tierInfo(1201));
        ScrimTeam nearSecond = createOpponentTeam("near-second", tierInfo(1203));
        ScrimTeam nearThird = createOpponentTeam("near-third", tierInfo(1206));

        List<ScrimTeam> fillers = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            fillers.add(createOpponentTeam("filler-" + i, tierInfo(1300 + i * 25)));
        }
        int totalTeams = 1 + 3 + fillers.size();

        em.flush();
        em.clear();

        // when
        ScrimTeamPageResponse result = scrimTeamReader.getScrimTeamList(leader.getId(), 0);

        // then
        assertThat(result.totalCount()).isEqualTo(totalTeams);
        assertThat(result.teams()).hasSize(10);

        List<ScrimTeamPageResponse.ScrimTeamResponse> recommended = result.teams().subList(0, 3);
        assertThat(recommended)
                .extracting(ScrimTeamPageResponse.ScrimTeamResponse::scrimTeamId)
                .containsExactly(nearFirst.getId(), nearSecond.getId(), nearThird.getId());

        assertThat(recommended)
                .allMatch(ScrimTeamPageResponse.ScrimTeamResponse::isRecommended);

        assertThat(recommended)
                .noneMatch(res -> res.scrimTeamId().equals(leaderTeam.getId()));

        boolean baseTeamsNotRecommended = result.teams().stream()
                .skip(3)
                .allMatch(team -> !team.isRecommended());
        assertThat(baseTeamsNotRecommended).isTrue();
    }

    private ScrimTeam createOpponentTeam(String name, TierInfo tierInfo) {
        User representative = persistUserWithTier(name + "@scrim.app", name, tierInfo);
        return createScrimTeam(name, representative, tierInfo);
    }

    private ScrimTeam createScrimTeam(String teamName, User representative, TierInfo tierInfo) {
        Set<ScrimTeamMember> members = new HashSet<>();
        LolPosition[] positions = {LolPosition.TOP, LolPosition.JUNGLE, LolPosition.MIDDLE, LolPosition.BOTTOM, LolPosition.UTILITY};
        members.add(new ScrimTeamMember(representative, positions[0]));
        for (int i = 1; i < positions.length; i++) {
            User member = persistUserWithTier(
                    "%s-member%d@scrim.app".formatted(teamName, i),
                    "%s-member%d".formatted(teamName, i),
                    tierInfo
            );
            members.add(new ScrimTeamMember(member, positions[i]));
        }

        ScrimTeam scrimTeam = ScrimTeam.create(new ScrimTeamCreateRequest(
                teamName,
                "intro-" + teamName,
                representative.getId(),
                members
        ));
        return scrimTeamRepository.save(scrimTeam);
    }

    private User persistUserWithTier(String email, String username, TierInfo tierInfo) {
        User user = UserFixture.createUser(email, username);
        user.completeUnivAuthentication(UserFixture.createEnrollUnivRequest("서울과학기술대학교", username + "@seoultech.ac.kr"));
        user.connectSummonerInfo(
                UserFixture.createSummonerInfoConnector(username + "-puuid", tierInfo),
                UserFixture.createRiotAuthRequest(username, "KR1")
        );
        return userRepository.save(user);
    }

    private TierInfo tierInfo(int mappedTier) {
        return new TierInfo(Tier.SILVER, Rank.I, 0, mappedTier);
    }
}
