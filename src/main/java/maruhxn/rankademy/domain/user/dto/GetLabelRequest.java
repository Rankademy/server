package maruhxn.rankademy.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;

public record GetLabelRequest(
        @NotEmpty
        String puuid, // 필수
        Integer count, // 선택(기본 10) 최근 N경기
        Integer topK // 선택(기본 3), 0이면 모두 반환
) {
}
