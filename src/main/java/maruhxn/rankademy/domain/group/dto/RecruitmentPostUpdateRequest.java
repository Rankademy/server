package maruhxn.rankademy.domain.group.dto;

import java.time.LocalDateTime;

public record RecruitmentPostUpdateRequest(
        String title,
        String content,
        String requirements,
        Integer capacity,
        LocalDateTime recruitmentStartDate,
        LocalDateTime recruitmentEndDate
) {
}
