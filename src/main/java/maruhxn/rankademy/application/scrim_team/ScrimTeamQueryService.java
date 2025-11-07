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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ScrimTeamPageResponse getScrimTeamList(Long userId, int page) {
        Long leaderScrimTeamId = null;
        if (userId != null) {
            Optional<ScrimTeam> optionalLeaderScrimTeam = scrimTeamQueryRepository.findMyLeaderTeamByUserId(userId);
            if (optionalLeaderScrimTeam.isPresent()) {
                leaderScrimTeamId = optionalLeaderScrimTeam.get().getId();
            }
        }

        List<ScrimTeamPageResponse.ScrimTeamResponse> recommendedTeams = List.of();
        List<Long> recommendedTeamIds = List.of();

        if (page == 0 && leaderScrimTeamId != null) {
            recommendedTeams = scrimTeamQueryRepository.findRecommendedTeams(leaderScrimTeamId, 3);
            recommendedTeamIds = recommendedTeams.stream()
                    .map(ScrimTeamPageResponse.ScrimTeamResponse::scrimTeamId)
                    .collect(Collectors.toList());
        }

        ScrimTeamPageResponse basePage = scrimTeamQueryRepository.findAll(page, recommendedTeamIds, recommendedTeams.size());

        if (page == 0 && !recommendedTeams.isEmpty()) {
            List<ScrimTeamPageResponse.ScrimTeamResponse> combined = new ArrayList<>(recommendedTeams.size() + basePage.teams().size());
            combined.addAll(recommendedTeams);
            combined.addAll(basePage.teams());
            return new ScrimTeamPageResponse(basePage.totalCount(), combined);
        }

        return basePage;
    }

    @Override
    public ScrimTeamDetailResponse getDetails(Long scrimTeamId) {
        return scrimTeamQueryRepository.getDetailById(scrimTeamId)
                .orElseThrow(() -> new NoSuchElementException("스크림 팀 정보를 찾을 수 없습니다. id: " + scrimTeamId));
    }
}
