package maruhxn.rankademy.adapter.integration.team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.required.TeamQueryRepository;
import maruhxn.rankademy.domain.team.service.TeamLeaderLimitValidator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultTeamLeaderLimitValidator implements TeamLeaderLimitValidator {

    private final TeamQueryRepository teamQueryRepository;

    @Override
    public void validateTeamLeaderLimitExceeded(Long userId) {
        if(teamQueryRepository.existsTeamLeaderByUserId(userId)) {
            throw new IllegalStateException("이미 리더인 팀이 있습니다.");
        }
    }
}
