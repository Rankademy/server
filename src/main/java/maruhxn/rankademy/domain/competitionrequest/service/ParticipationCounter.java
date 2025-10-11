package maruhxn.rankademy.domain.competitionrequest.service;

import java.time.LocalDateTime;

public interface ParticipationCounter {

    int countUserParticipation(Long userId, LocalDateTime windowStart, LocalDateTime windowEnd);
}
