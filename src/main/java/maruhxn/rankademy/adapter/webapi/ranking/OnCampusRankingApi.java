package maruhxn.rankademy.adapter.webapi.ranking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.persistence.ranking.OnCampusRankingRepository;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.application.group.provided.dto.GroupResponse;
import maruhxn.rankademy.domain.user.LolPosition;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rankings/univ/{univName}")
@RequiredArgsConstructor
@Tag(name = "On-campus Rankings", description = "대학교별 랭킹 조회 API")
public class OnCampusRankingApi {

    private final OnCampusRankingRepository onCampusRankingRepository;

    @GetMapping
    @Operation(
            summary = "학생 랭킹 조회",
            description = "대학교 내 학생 랭킹을 다양한 필터로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "학생 랭킹 조회 성공")
    public PagedModel<UnivStudentRankingResponse> getUnivStudentRanking(
            @Parameter(description = "대학교 이름", example = "서울과학기술대학교")
            @PathVariable("univName") String univName,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "전공명 필터")
            @RequestParam(value = "major", required = false) String major,
            @Parameter(description = "입학년도 필터", example = "2021")
            @RequestParam(value = "admissionYear", required = false) Integer admissionYear,
            @Parameter(description = "주 포지션 필터", schema = @Schema(implementation = LolPosition.class))
            @RequestParam(value = "mainPosition", required = false) LolPosition mainPosition,
            @RequestParam(value = "userNameKey", required = false) String userNameKey
    ) {
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(major, admissionYear, mainPosition, userNameKey);
        return onCampusRankingRepository.getUnivStudentRanking(univName, page, univStudentRankingFilter);
    }

    @GetMapping("/groups")
    @Operation(
            summary = "학교별 그룹 랭킹 조회",
            description = "대학교 내 그룹 랭킹을 정렬 및 필터 조건과 함께 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "그룹 랭킹 조회 성공")
    public PagedModel<GroupResponse> getGroupRankingList(
            @Parameter(description = "대학교 이름", example = "서울과학기술대학교")
            @PathVariable("univName") String univName,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page,
            @Parameter(description = "그룹명 검색 키워드")
            @RequestParam(value = "groupNameKey", required = false) String groupNameKey
    ) {
        return onCampusRankingRepository.getGroupRanking(univName, page, groupNameKey);
    }
}
