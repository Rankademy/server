package maruhxn.rankademy.application.team.dto.request;

import org.springframework.util.Assert;

import java.util.List;

public record AdjustMmrRequest(
        List<MemberDto> winner,
        List<MemberDto> loser
) {
    public AdjustMmrRequest {
        Assert.state(winner != null && !winner.isEmpty(), "승리팀 정보를 전달해주세요");
        Assert.state(loser != null && !loser.isEmpty(), "패배팀 정보를 전달해주세요");
    }
}
