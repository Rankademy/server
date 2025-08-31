package maruhxn.rankademy.domain.competition.dto;

import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

public record SubmitCompetitionResultRequest(
        Long team1Id,
        Long team2Id,
        int totalSets,
        List<SetResultDto> setResults,
        String memo,
        Long finalWinnerId
) {

    public SubmitCompetitionResultRequest {
        Assert.isTrue(setResults != null && !setResults.isEmpty(), "세트 정보는 비어있을 수 없습니다.");
        Assert.isTrue(totalSets == setResults.size(), "진행한 세트 수와 경기 데이터 수가 다릅니다.");
        Assert.isTrue(setCount(setResults) == totalSets, "세트 번호 중복입니다.");
    }

    private int setCount(List<SetResultDto> setResults) {
        return setResults.stream()
                .map(SetResultDto::setNumber)
                .collect(Collectors.toSet()).size();
    }

    public record SetResultDto(
            int setNumber,
            Long winnerTeamId,
            String resultImageKey
    ) {

    }
}
