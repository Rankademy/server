package maruhxn.rankademy.domain.competitionrequest.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Getter
@NoArgsConstructor
public class WeeklyLimitPolicy {

    private int maxPerWeek = 3;
    private DayOfWeek resetDay = DayOfWeek.MONDAY;
    private LocalTime resetTime = LocalTime.of(1, 0, 0);

    // "매주 월요일 01:00" 같은 리셋 포인트를 기준으로 가장 최근 리셋 시각 계산
    public LocalDateTime currentWindowStart(LocalDateTime now) {
        LocalDateTime candidate = now.with(resetDay).withHour(resetTime.getHour())
                .withMinute(resetTime.getMinute()).withSecond(resetTime.getSecond()).withNano(0);
        if (candidate.isAfter(now)) candidate = candidate.minusWeeks(1);
        return candidate;
    }
}