package maruhxn.rankademy.domain.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "대항전 결과 제출 요청")
public record SubmitCompetitionResultRequest(
        @Schema(description = "팀1 ID", example = "1") Long team1Id,
        @Schema(description = "팀2 ID", example = "2") Long team2Id,
        @Schema(description = "세트 수", example = "3") int totalSets,
        @Schema(description = "세트별 결과 목록") List<SetResultDto> setResults,
        @Schema(description = "비고 메모") String memo,
        @Schema(description = "최종 승리 팀 ID", example = "1") Long finalWinnerId,
        @Schema(description = "승리 그룹 ID", example = "10") Long finalWinnerGroupId,
        @Schema(description = "패배 그룹 ID", example = "11") Long finalLoserGroupId
) {

    public SubmitCompetitionResultRequest {
        Assert.isTrue(setResults != null && !setResults.isEmpty(), "세트 정보는 비어있을 수 없습니다.");
        Assert.isTrue(totalSets == setResults.size(), "진행한 세트 수와 경기 데이터 수가 다릅니다.");
        Assert.isTrue(setCount(setResults) == totalSets, "세트 번호 중복입니다.");
        Assert.isTrue(!finalWinnerGroupId.equals(finalLoserGroupId), "승자 그룹과 패배자 그룹은 서로 같을 수 없습니다.");
    }

    private int setCount(List<SetResultDto> setResults) {
        return setResults.stream()
                .map(SetResultDto::setNumber)
                .collect(Collectors.toSet()).size();
    }

    @Schema(description = "세트 결과 입력")
    public record SetResultDto(
            @Schema(description = "세트 번호", example = "1") int setNumber,
            @Schema(description = "세트 승리 팀 ID", example = "1") Long winnerTeamId,
            @Schema(description = "영상 또는 증빙 이미지 키") String resultImageKey
    ) {

    }
}
