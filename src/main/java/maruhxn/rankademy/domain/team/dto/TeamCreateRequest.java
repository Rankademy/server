package maruhxn.rankademy.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;

import java.util.Set;

@Schema(description = "팀 생성 요청 바디")
public record TeamCreateRequest(
        @Schema(description = "팀이 속한 그룹 ID", example = "1") Long groupId,
        @Schema(description = "팀 이름", example = "Rankademy Team") String name,
        @Schema(description = "팀 소개", example = "Rankademy 정식 팀입니다") String intro,
        @Schema(description = "대표자 사용자 ID", example = "100") Long representativeId,
        @Schema(description = "팀원 슬롯 정보") Set<TeamMemberSlot> members
) {
    @Schema(description = "팀 멤버 구성 정보")
    public record TeamMemberSlot(
            @Schema(description = "사용자 ID", example = "101") Long userId,
            @Schema(description = "역할 포지션", implementation = LolPosition.class) LolPosition position
    ) {
    }
}
