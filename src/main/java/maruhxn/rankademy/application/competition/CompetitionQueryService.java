package maruhxn.rankademy.application.competition;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competition.provided.CompetitionReader;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionDetailResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;
import maruhxn.rankademy.application.competition.required.CompetitionQueryRepository;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.required.TeamQueryRepository;
import maruhxn.rankademy.domain.competition.Competition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompetitionQueryService implements CompetitionReader {

    private final TeamQueryRepository teamQueryRepository;
    private final CompetitionRepository competitionRepository;
    private final CompetitionQueryRepository competitionQueryRepository;

    @Override
    public CompetitionDetailResponse getDetail(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("대항전 정보를 찾을 수 없습니다. competitionId: " + id));
        TeamDetailResponse team1Detail = teamQueryRepository.getDetailById(competition.getTeam1Id())
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. team1Id: " + competition.getTeam1Id()));
        TeamDetailResponse team2Detail = teamQueryRepository.getDetailById(competition.getTeam2Id())
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. team2Id: " + competition.getTeam2Id()));

        return new CompetitionDetailResponse(
                id,
                competition.getStatus(),
                team1Detail,
                team2Detail
        );
    }

    @Override
    public CompetitionResultResponse getResult(Long id) {
        return competitionQueryRepository.getResult(id);
    }
}
