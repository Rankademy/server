package maruhxn.rankademy.adapter.integration;

import maruhxn.rankademy.domain.competitionrequest.service.ParticipationCounter;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Fallback
public class DummyParticipationCounter implements ParticipationCounter {
    @Override
    public int countUserParticipation(Long userId, LocalDateTime windowStart, LocalDateTime windowEnd) {
        return 0;
    }
}
