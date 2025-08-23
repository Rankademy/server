package maruhxn.rankademy.domain.competition.dto;

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
        if (totalSets != setResults.size()) {
            throw new IllegalArgumentException("진행한 세트 수와 경기 데이터 수가 다릅니다.");
        }

        int setCount = setResults.stream().map(SetResultDto::setNumber).collect(Collectors.toSet()).size();
        if (setCount != totalSets) throw new IllegalArgumentException("세트 번호 중복입니다.");
    }

    public record SetResultDto(
            int setNumber,
            Long winnerTeamId,
            String resultImageKey
    ) {

    }
}
