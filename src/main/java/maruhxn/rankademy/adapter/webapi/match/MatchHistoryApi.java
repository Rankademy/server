package maruhxn.rankademy.adapter.webapi.match;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.match.provided.MatchHistoryAnalyzer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me/matches")
//@Tag(name = "Matches", description = "매치 전적 관리 API")
@Hidden
public class MatchHistoryApi {

    private final MatchHistoryAnalyzer matchHistoryAnalyzer;

    @Deprecated
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "전적 새로고침",
            description = "마지막으로 동기화한 이후의 매치를 갱신합니다."
    )
    @ApiResponse(responseCode = "201", description = "전적 갱신 성공")
    public void refreshMatches(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        matchHistoryAnalyzer.refreshMatches(rankademyUser.getId());
    }
}
