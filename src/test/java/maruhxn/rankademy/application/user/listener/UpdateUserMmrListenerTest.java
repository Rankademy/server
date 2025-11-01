package maruhxn.rankademy.application.user.listener;

import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.team.dto.request.AdjustMmrRequest;
import maruhxn.rankademy.application.team.dto.request.MemberDto;
import maruhxn.rankademy.application.team.dto.response.AdjustedMmrResponse;
import maruhxn.rankademy.application.team.required.MatchMakingClient;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.shared.event.CompetitionResultSubmitEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.EffectiveStrength;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@DisplayName("애플리케이션 - 유저 MMR 업데이트 리스너 (SpringBootTest)")
class UpdateUserMmrListenerTest extends IntegrationTestSupport {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UpdateUserMmrListener updateUserMmrListener;

    @MockitoBean
    private MatchMakingClient matchMakingClient;

    @Test
    @DisplayName("대항전 결과 반영 시 매치메이킹 응답으로 유저 실력을 갱신한다.")
    void on_updatesEffectiveStrengthForMatchedPlayers() {
        // given
        TeamContext winnerContext = createTeamContext("winner", 101L);
        TeamContext loserContext = createTeamContext("loser", 202L);

        Competition competition = prepareCompetition(winnerContext.teamId(), loserContext.teamId());
        Long competitionId = competitionRepository.save(competition).getId();

        Map<Long, StrengthSnapshot> expectedStrength = new HashMap<>();
        List<AdjustedMmrResponse.PlayerMmr> adjustedPlayers = new ArrayList<>();

        winnerContext.puuidToUserId().forEach((puuid, userId) -> {
            StrengthSnapshot base = winnerContext.initialStrength().get(userId);
            double newMu = base.mu() + 4.2;
            double newSigma = Math.max(1.0, base.sigma() - 0.3);
            expectedStrength.put(userId, new StrengthSnapshot(newMu, newSigma));
            adjustedPlayers.add(new AdjustedMmrResponse.PlayerMmr(puuid, newMu, newSigma));
        });

        loserContext.puuidToUserId().forEach((puuid, userId) -> {
            StrengthSnapshot base = loserContext.initialStrength().get(userId);
            double newMu = base.mu() - 2.5;
            double newSigma = base.sigma() + 0.4;
            expectedStrength.put(userId, new StrengthSnapshot(newMu, newSigma));
            adjustedPlayers.add(new AdjustedMmrResponse.PlayerMmr(puuid, newMu, newSigma));
        });

        given(matchMakingClient.adjustUsersMmr(any(AdjustMmrRequest.class)))
                .willReturn(new AdjustedMmrResponse(adjustedPlayers));

        CompetitionResultSubmitEvent event = new CompetitionResultSubmitEvent(competitionId, 99L);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        updateUserMmrListener.on(event);

        // then
        ArgumentCaptor<AdjustMmrRequest> requestCaptor = ArgumentCaptor.forClass(AdjustMmrRequest.class);
        verify(matchMakingClient).adjustUsersMmr(requestCaptor.capture());
        AdjustMmrRequest capturedRequest = requestCaptor.getValue();

        assertThat(extractPuuids(capturedRequest.winner()))
                .containsExactlyInAnyOrderElementsOf(winnerContext.puuidToUserId().keySet());
        assertThat(extractPuuids(capturedRequest.loser()))
                .containsExactlyInAnyOrderElementsOf(loserContext.puuidToUserId().keySet());

        Map<Long, StrengthSnapshot> actualStrength = loadStrengths(Stream.of(winnerContext, loserContext)
                .flatMap(ctx -> ctx.puuidToUserId().values().stream())
                .collect(Collectors.toSet()));

        assertThat(actualStrength.keySet()).containsExactlyInAnyOrderElementsOf(expectedStrength.keySet());
        expectedStrength.forEach((userId, snapshot) -> {
            StrengthSnapshot actual = actualStrength.get(userId);
            assertThat(actual.mu()).isEqualTo(snapshot.mu());
            assertThat(actual.sigma()).isEqualTo(snapshot.sigma());
        });
    }

    @Test
    @DisplayName("매치메이킹 응답에 일치하는 플레이어가 없다면 유저 실력을 변경하지 않는다.")
    void on_doesNotUpdateWhenPlayersDoNotMatch() {
        // given
        TeamContext winnerContext = createTeamContext("winner-no-update", 301L);
        TeamContext loserContext = createTeamContext("loser-no-update", 302L);

        Competition competition = prepareCompetition(winnerContext.teamId(), loserContext.teamId());
        Long competitionId = competitionRepository.save(competition).getId();

        Map<Long, StrengthSnapshot> initialStrengths = Stream.of(winnerContext, loserContext)
                .flatMap(ctx -> ctx.initialStrength().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        given(matchMakingClient.adjustUsersMmr(any(AdjustMmrRequest.class)))
                .willReturn(new AdjustedMmrResponse(List.of(
                        new AdjustedMmrResponse.PlayerMmr("irrelevant-puuid", 45.0, 3.0)
                )));

        CompetitionResultSubmitEvent event = new CompetitionResultSubmitEvent(competitionId, 88L);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        updateUserMmrListener.on(event);

        // then
        Map<Long, StrengthSnapshot> actualStrength = loadStrengths(initialStrengths.keySet());
        assertThat(actualStrength.keySet()).containsExactlyInAnyOrderElementsOf(initialStrengths.keySet());
        initialStrengths.forEach((userId, snapshot) -> {
            StrengthSnapshot actual = actualStrength.get(userId);
            assertThat(actual.mu()).isEqualTo(snapshot.mu());
            assertThat(actual.sigma()).isEqualTo(snapshot.sigma());
        });

        verify(matchMakingClient).adjustUsersMmr(any(AdjustMmrRequest.class));
        verifyNoMoreInteractions(matchMakingClient);
    }

    private TeamContext createTeamContext(String prefix, long groupId) {
        LolPosition[] positions = LolPosition.values();
        Set<TeamMember> teamMembers = new HashSet<>();
        Map<String, Long> puuidToUserId = new LinkedHashMap<>();
        Map<Long, StrengthSnapshot> initialStrength = new LinkedHashMap<>();
        Long representativeId = null;

        for (int i = 0; i < positions.length; i++) {
            String email = "%s%d@rankademy.test".formatted(prefix, i);
            String username = "%s-user-%d".formatted(prefix, i);

            User user = UserFixture.createAuthorizedMember(email, username);
            double mu = 25.0 + i;
            double sigma = 6.0 - (i * 0.2);
            user.updateEffectiveStrength(new EffectiveStrength(mu, sigma));

            user = userRepository.save(user);

            if (representativeId == null) {
                representativeId = user.getId();
            }

            String puuid = user.getSummonerInfo().getPuuid();
            puuidToUserId.put(puuid, user.getId());
            initialStrength.put(user.getId(), new StrengthSnapshot(mu, sigma));
            teamMembers.add(new TeamMember(user, positions[i]));
        }

        Team team = new Team(groupId, prefix + "-team", prefix + "-intro", representativeId);
        team.setTeamMembers(teamMembers);

        team = teamRepository.save(team);

        return new TeamContext(team.getId(), puuidToUserId, initialStrength);
    }

    private Competition prepareCompetition(Long team1Id, Long team2Id) {
        Competition competition = Competition.createAfterAccept(team1Id, team2Id);

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1Id,
                team2Id,
                3,
                List.of(
                        new SubmitCompetitionResultRequest.SetResultDto(1, team1Id, "set-1"),
                        new SubmitCompetitionResultRequest.SetResultDto(2, team2Id, "set-2"),
                        new SubmitCompetitionResultRequest.SetResultDto(3, team1Id, "set-3")
                ),
                "final result",
                team1Id,
                1_001L,
                2_001L
        );

        competition.submitSetResult(request, LocalDateTime.now());
        return competition;
    }

    private Map<Long, StrengthSnapshot> loadStrengths(Collection<Long> userIds) {
        return userIds.stream()
                .map(userId -> userRepository.findById(userId).orElseThrow())
                .collect(Collectors.toMap(
                        User::getId,
                        user -> new StrengthSnapshot(
                                user.getEffectiveStrength().getMu(),
                                user.getEffectiveStrength().getSigma()
                        )
                ));
    }

    private List<String> extractPuuids(List<MemberDto> members) {
        return members.stream()
                .map(MemberDto::puuid)
                .toList();
    }

    private record TeamContext(
            Long teamId,
            Map<String, Long> puuidToUserId,
            Map<Long, StrengthSnapshot> initialStrength
    ) {
    }

    private record StrengthSnapshot(double mu, double sigma) {
    }
}
