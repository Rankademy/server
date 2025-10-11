package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내 그룹 요약 정보")
public record MyGroupResponse(
        @Schema(description = "그룹 ID", example = "1") Long groupId,
        @Schema(description = "그룹 이름", example = "Rankademy 그룹") String groupName,
        @Schema(description = "그룹 로고 이미지 URL") String groupLogoImg,
        @Schema(description = "그룹 소개") String about,
        @Schema(description = "그룹 생성일") LocalDateTime createdAt
) {
}
