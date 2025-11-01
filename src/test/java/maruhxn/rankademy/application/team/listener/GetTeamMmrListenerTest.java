package maruhxn.rankademy.application.team.listener;

import maruhxn.rankademy.application.team.dto.request.GetTeamMmrRequest;
import maruhxn.rankademy.application.team.dto.request.MemberDto;
import maruhxn.rankademy.application.team.dto.response.TeamMmrResponse;
import maruhxn.rankademy.application.team.required.MatchMakingClient;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.shared.event.TeamCreatedEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("애플리케이션 - 팀 MMR 조회 리스너 (SpringBootTest)")
class GetTeamMmrListenerTest extends IntegrationTestSupport {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GetTeamMmrListener getTeamMmrListener;

    @MockitoBean
    private MatchMakingClient matchMakingClient;

    @Test
    @DisplayName("팀 생성 이벤트 발생 시 매치메이킹 서비스로 팀 MMR을 조회하고 팀 평균 MMR을 갱신한다.")
    void on_updatesTeamAverageMmr() {
        // given
        TeamSetup teamSetup = createTeam("listener-team", 601L);
        TeamCreatedEvent event = new TeamCreatedEvent(teamSetup.teamId(), LocalDateTime.now());

        double expectedAvgMmr = 37.42;
        given(matchMakingClient.getTeamMmr(any(GetTeamMmrRequest.class)))
                .willReturn(new TeamMmrResponse(expectedAvgMmr));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        getTeamMmrListener.on(event);

        // then
        ArgumentCaptor<GetTeamMmrRequest> requestCaptor = ArgumentCaptor.forClass(GetTeamMmrRequest.class);
        verify(matchMakingClient).getTeamMmr(requestCaptor.capture());
        GetTeamMmrRequest request = requestCaptor.getValue();

        Set<String> requestPuuids = request.members().stream()
                .map(MemberDto::puuid)
                .collect(Collectors.toSet());
        assertThat(request.members()).hasSize(teamSetup.memberPuuids().size());
        assertThat(requestPuuids).containsExactlyInAnyOrderElementsOf(teamSetup.memberPuuids());

        Team reloadedTeam = teamRepository.findById(teamSetup.teamId()).orElseThrow();
        assertThat(reloadedTeam.getAvgMmr()).isEqualTo(expectedAvgMmr);
    }

    private TeamSetup createTeam(String prefix, long groupId) {
        LolPosition[] positions = LolPosition.values();
        Set<TeamMember> members = new HashSet<>();
        Set<String> puuids = new HashSet<>();

        User representative = null;

        for (int i = 0; i < positions.length; i++) {
            String email = "%s-%d@rankademy.test".formatted(prefix, i);
            String username = "%s-user-%d".formatted(prefix, i);

            User user = UserFixture.createAuthorizedMember(email, username);
            user = userRepository.save(user);

            if (representative == null) {
                representative = user;
            }

            puuids.add(user.getSummonerInfo().getPuuid());
            members.add(new TeamMember(user, positions[i]));
        }

        Team team = new Team(groupId, prefix, prefix + "-intro", representative.getId());
        team.setTeamMembers(members);
        team = teamRepository.save(team);

        return new TeamSetup(team.getId(), puuids);
    }

    private record TeamSetup(Long teamId, Set<String> memberPuuids) {
    }
}
