package maruhxn.rankademy.domain.group.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecruitmentPostCreateRequest(
        String title,
        String content,
        String requirements,
        Integer capacity,
        LocalDateTime recruitmentStartDate,
        LocalDateTime recruitmentEndDate
) {
}
