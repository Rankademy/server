package maruhxn.rankademy.application.competition;

import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.shared.event.CompetitionAcceptEvent;
import maruhxn.rankademy.domain.team.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompetitionRequestAcceptListener 테스트")
class CompetitionRequestAcceptListenerTest {

    @InjectMocks
    private CompetitionRequestAcceptListener competitionRequestAcceptListener;

    @Mock
    private CompetitionRepository competitionRepository;

    @Test
    @DisplayName("대항전 생성 이벤트 수신 시 대항전이 정상적으로 생성된다")
    void createCompetition() {
        // given
        long fromTeamId = 1L;
        long toTeamId = 2L;
        CompetitionAcceptEvent event = new CompetitionAcceptEvent(fromTeamId, toTeamId, LocalDateTime.now());

        // when
        competitionRequestAcceptListener.createCompetition(event);

        // then
        ArgumentCaptor<Competition> competitionCaptor = ArgumentCaptor.forClass(Competition.class);
        verify(competitionRepository, times(1)).save(competitionCaptor.capture());

        Competition savedCompetition = competitionCaptor.getValue();
        assertThat(savedCompetition.getTeam1Id()).isEqualTo(fromTeamId);
        assertThat(savedCompetition.getTeam2Id()).isEqualTo(toTeamId);
    }

}