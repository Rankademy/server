package maruhxn.rankademy.application.team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.provided.TeamReader;
import maruhxn.rankademy.application.team.provided.dto.MyTeamPageResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.application.team.required.TeamQueryRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
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
public class TeamQueryService implements TeamReader {

    private final TeamRepository teamRepository;
    private final TeamQueryRepository teamQueryRepository;

    @Override
    public Team get(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("팀을 찾을 수 없습니다. id: " + teamId));
    }

    @Override
    public TeamPageResponse getTeamList(Long userId, int page) {
        Optional<Team> optionalLeaderTeam = teamQueryRepository.findMyLeaderTeamByUserId(userId);
        Long leaderTeamId = null;
        if (optionalLeaderTeam.isPresent()) {
            leaderTeamId = optionalLeaderTeam.get().getId();
        }
        List<TeamPageResponse.TeamResponse> recommendedTeams = List.of();
        List<Long> recommendedTeamIds = List.of();

        if (page == 0 && leaderTeamId != null) {
            recommendedTeams = teamQueryRepository.findRecommendedTeams(leaderTeamId, 3);
            recommendedTeamIds = recommendedTeams.stream()
                    .map(TeamPageResponse.TeamResponse::teamId)
                    .collect(Collectors.toList());
        }

        TeamPageResponse basePage = teamQueryRepository.findAll(page, recommendedTeamIds, recommendedTeams.size());

        if (page == 0 && !recommendedTeams.isEmpty()) {
            List<TeamPageResponse.TeamResponse> combined = new ArrayList<>(recommendedTeams.size() + basePage.teams().size());
            combined.addAll(recommendedTeams);
            combined.addAll(basePage.teams());
            return new TeamPageResponse(basePage.totalCount(), combined);
        }

        return basePage;
    }

    @Override
    public PagedModel<MyTeamPageResponse> getMyTeamList(Long userId, int page) {
        Page<MyTeamPageResponse> result = teamQueryRepository.findMyTeamList(userId, page);
        return new PagedModel<>(result);
    }

    @Override
    public TeamDetailResponse getTeamDetails(Long userId, Long teamId) {
        return teamQueryRepository.getDetailById(userId, teamId)
                .orElseThrow(() -> new NoSuchElementException("팀을 찾을 수 없습니다. id: " + teamId));
    }

}
