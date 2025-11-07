package maruhxn.rankademy.adapter.integration.scrim_team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.scrim_team.required.ScrimTeamQueryRepository;
import maruhxn.rankademy.domain.scrim_team.service.ScrimTeamLeaderLimitValidator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultScrimTeamLeaderLimitValidator implements ScrimTeamLeaderLimitValidator {

    private final ScrimTeamQueryRepository scrimTeamQueryRepository;

    @Override
    public void validateScrimTeamLeaderLimitExceeded(Long userId) {
        if(scrimTeamQueryRepository.existsTeamLeaderByUserId(userId)) {
            throw new IllegalStateException("이미 리더인 스크림 팀이 있습니다.");
        }
    }
}
