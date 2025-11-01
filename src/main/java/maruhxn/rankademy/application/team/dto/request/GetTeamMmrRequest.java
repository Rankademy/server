package maruhxn.rankademy.application.team.dto.request;

import org.springframework.util.Assert;

import java.util.List;

public record GetTeamMmrRequest(
        List<MemberDto> members
) {
    public GetTeamMmrRequest {
        Assert.state(members != null && !members.isEmpty(), "멤버 리스트를 전달해주세요.");
    }
}
