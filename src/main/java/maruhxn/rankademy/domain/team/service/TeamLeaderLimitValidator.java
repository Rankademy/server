package maruhxn.rankademy.domain.team.service;

public interface TeamLeaderLimitValidator {
    void validateTeamLeaderLimitExceeded(Long userId);
}
