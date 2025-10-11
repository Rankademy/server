package maruhxn.rankademy.adapter.webapi.ranking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.persistence.ranking.TotalRankingRepository;
import maruhxn.rankademy.adapter.webapi.dto.TotalUserRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
@Tag(name = "Global Rankings", description = "전체 랭킹 조회 API")
public class TotalRankingApi {

    private final TotalRankingRepository totalRankingRepository;

    @GetMapping("/univ")
    @Operation(
            summary = "대학교 랭킹 조회",
            description = "전체 대학교 랭킹을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "대학교 랭킹 조회 성공")
    public List<UnivRankingResponse> getUnivRanking(
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "대학교명 검색 키워드")
            @RequestParam(value = "univNameKey", required = false) String univNameKey
    ) {
        return totalRankingRepository.getUnivRanking(page, univNameKey);
    }

    @GetMapping("/users")
    @Operation(
            summary = "유저 랭킹 조회",
            description = "전체 유저 랭킹을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "유저 랭킹 조회 성공")
    public PagedModel<TotalUserRankingResponse> getTotalUserRanking(
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "유저명 검색 키워드")
            @RequestParam(value = "userNameKey", required = false) String userNameKey
    ) {
        return totalRankingRepository.getTotalUserRanking(page, userNameKey);
    }
}
