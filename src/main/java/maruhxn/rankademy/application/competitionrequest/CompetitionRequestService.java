package maruhxn.rankademy.application.competitionrequest;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competitionrequest.provided.CompetitionRequestManager;
import maruhxn.rankademy.application.competitionrequest.required.CompetitionRequestRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import maruhxn.rankademy.domain.competitionrequest.service.CompetitionRequestAcceptor;
import maruhxn.rankademy.domain.competitionrequest.service.CompetitionRequestSender;
import maruhxn.rankademy.domain.shared.event.SendCompetitionRequestEvent;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class CompetitionRequestService implements CompetitionRequestManager {

    private final CompetitionRequestSender sender;
    private final TeamRepository teamRepository;
    private final CompetitionRequestRepository competitionRequestRepository;
    private final CompetitionRequestAcceptor competitionRequestAcceptor;
    private final ApplicationEventPublisher publisher;

    @Override
    public CompetitionRequest sendRequest(Long actingUserId, Long fromTeamId, Long toTeamId) {
        Team from = teamRepository.findById(fromTeamId)
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. fromTeamId: " + fromTeamId));
        Team to = teamRepository.findById(toTeamId)
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. toTeamId: " + toTeamId));

        LocalDateTime now = LocalDateTime.now();
        publisher.publishEvent(new SendCompetitionRequestEvent(from.getId(), to.getId(), actingUserId, now));
        return sender.sendRequest(from, to, actingUserId, now);
    }

    @Override
    public void acceptRequest(Long actingUserId, Long requestId) {
        CompetitionRequest competitionRequest = competitionRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("대항전 요청 정보를 찾을 수 없습니다. requestId: " + requestId));
        competitionRequestAcceptor.accept(competitionRequest);
    }

    @Override
    public void rejectRequest(Long actingUserId, Long requestId) {
        CompetitionRequest competitionRequest = competitionRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("대항전 요청 정보를 찾을 수 없습니다. requestId: " + requestId));
        competitionRequest.reject();
    }
}
