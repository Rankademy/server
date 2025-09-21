package maruhxn.rankademy.application.scrim_team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.scrim_team.provided.ScrimTeamReader;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamDetailResponse;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamPageResponse;
import maruhxn.rankademy.application.scrim_team.required.ScrimTeamQueryRepository;
import maruhxn.rankademy.application.scrim_team.required.ScrimTeamRepository;
import maruhxn.rankademy.domain.scrim_team.ScrimTeam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScrimTeamQueryService implements ScrimTeamReader {

    private final ScrimTeamRepository scrimTeamRepository;
    private final ScrimTeamQueryRepository scrimTeamQueryRepository;

    @Override
    public ScrimTeam get(Long scrimTeamId) {
        return scrimTeamRepository.findById(scrimTeamId)
                .orElseThrow(() -> new NoSuchElementException("스크림 팀 정보를 찾을 수 없습니다. id: " + scrimTeamId));
    }

    @Override
    public ScrimTeamPageResponse getScrimTeamList(int page) {
        return scrimTeamQueryRepository.findAll(page);
    }

    @Override
    public ScrimTeamDetailResponse getDetails(Long scrimTeamId) {
        return scrimTeamQueryRepository.getDetailById(scrimTeamId)
                .orElseThrow(() -> new NoSuchElementException("스크림 팀 정보를 찾을 수 없습니다. id: " + scrimTeamId));
    }
}
