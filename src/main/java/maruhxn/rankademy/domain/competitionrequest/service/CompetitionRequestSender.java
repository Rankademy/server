package maruhxn.rankademy.domain.competitionrequest.service;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompetitionRequestSender {

    private final WeeklyLimitPolicy policy;
    private final ParticipationCounter counter;

    public CompetitionRequest sendRequest(Team from, Team to, Long actingUser, LocalDateTime now) {
        if (!from.isRepresentative(actingUser))
            throw new IllegalArgumentException("대표자만 대항전을 요청할 수 있습니다.");
        if (from.getGroupId().equals(to.getGroupId()))
            throw new IllegalArgumentException("같은 그룹끼리는 대항전 요청을 보낼 수 없습니다.");

        // 주당 N회 제한: 요청 시점에 참여 제한을 미리 가드(요청/수락/진행 모두에 적용할 수도 있음)
        var windowStart = policy.currentWindowStart(now);
        int participationCount = counter.countUserParticipation(actingUser, windowStart, now);
        if (participationCount > policy.getMaxPerWeek())
            throw new IllegalStateException("주간 참여 가능 횟수를 초과했습니다.");

        return new CompetitionRequest(from.getId(), to.getId(), now);
    }
}
