package maruhxn.rankademy.application.team.listener;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.dto.request.GetTeamMmrRequest;
import maruhxn.rankademy.application.team.dto.request.MemberDto;
import maruhxn.rankademy.application.team.dto.response.TeamMmrResponse;
import maruhxn.rankademy.application.team.provided.TeamReader;
import maruhxn.rankademy.application.team.required.MatchMakingClient;
import maruhxn.rankademy.domain.shared.event.TeamCreatedEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetTeamMmrListener {

    private final TeamReader teamReader;
    private final MatchMakingClient matchMakingClient;

    /**
     * 팀 생성 시, 팀 MMR 조회
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TeamCreatedEvent event) {
        Team team = teamReader.get(event.getTeamId());
        List<MemberDto> members = team.getTeamMembers().stream()
                .map(TeamMember::getUser)
                .map(MemberDto::from)
                .toList();

        TeamMmrResponse response = matchMakingClient.getTeamMmr(new GetTeamMmrRequest(members));
        team.updateTeamMmr(response.teamAvg());
    }
}
