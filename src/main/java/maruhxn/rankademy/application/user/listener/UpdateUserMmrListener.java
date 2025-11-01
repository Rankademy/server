package maruhxn.rankademy.application.user.listener;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.team.dto.request.AdjustMmrRequest;
import maruhxn.rankademy.application.team.dto.request.MemberDto;
import maruhxn.rankademy.application.team.dto.response.AdjustedMmrResponse;
import maruhxn.rankademy.application.team.required.MatchMakingClient;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.shared.event.CompetitionResultSubmitEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.EffectiveStrength;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UpdateUserMmrListener {

    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final MatchMakingClient matchMakingClient;

    /**
     * 대항전 진행 완료 시, 속한 유저들 MMR 업데이트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CompetitionResultSubmitEvent event) {
        Competition competition = competitionRepository.findById(event.getCompetitionId())
                .orElseThrow(() -> new NoSuchElementException("대항전 정보를 찾을 수 없습니다. compeitionId: " + event.getCompetitionId()));

        Team team1 = getTeam(competition.getTeam1Id());
        Team team2 = getTeam(competition.getTeam2Id());
        Team winnerTeam = resolveWinnerTeam(competition, team1, team2);
        Team loserTeam = winnerTeam.equals(team1) ? team2 : team1;

        AdjustMmrRequest adjustMmrRequest = new AdjustMmrRequest(
                toMemberDtos(winnerTeam),
                toMemberDtos(loserTeam)
        );

        AdjustedMmrResponse adjustedMmrResponse = matchMakingClient.adjustUsersMmr(adjustMmrRequest);
        applyAdjustedMmr(team1, team2, adjustedMmrResponse);
    }

    private Team getTeam(Long teamId) {
        return teamRepository.findByIdWithTeamMember(teamId)
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + teamId));
    }

    private Team resolveWinnerTeam(Competition competition, Team team1, Team team2) {
        Long winnerTeamId = competition.getFinalWinnerId();

        if (winnerTeamId.equals(team1.getId())) {
            return team1;
        }

        if (winnerTeamId.equals(team2.getId())) {
            return team2;
        }

        throw new IllegalStateException("대항전 결과에 승자 팀이 없습니다. competitionId: " + competition.getId());
    }

    private List<MemberDto> toMemberDtos(Team team) {
        return team.getTeamMembers().stream()
                .map(teamMember -> MemberDto.from(teamMember.getUser()))
                .toList();
    }

    private void applyAdjustedMmr(Team team1, Team team2, AdjustedMmrResponse adjustedMmrResponse) {
        if (adjustedMmrResponse == null || adjustedMmrResponse.players() == null) {
            return;
        }

        Map<String, User> userByPuuid = Stream.concat(team1.getTeamMembers().stream(), team2.getTeamMembers().stream())
                .map(TeamMember::getUser)
                .filter(user -> user.getSummonerInfo() != null)
                .collect(Collectors.toMap(
                        user -> user.getSummonerInfo().getPuuid(),
                        Function.identity(),
                        (existing, ignored) -> existing
                ));

        adjustedMmrResponse.players()
                .forEach(player -> {
                    User user = userByPuuid.get(player.puuid());
                    if (user == null) {
                        return;
                    }
                    user.updateEffectiveStrength(new EffectiveStrength(player.mu(), player.sigma()));
                });
    }
}
