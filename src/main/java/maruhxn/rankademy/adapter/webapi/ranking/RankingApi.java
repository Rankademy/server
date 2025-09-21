package maruhxn.rankademy.adapter.webapi.ranking;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.persistence.UnivRankingRepository;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.domain.user.LolPosition;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rankings/univ")
@RequiredArgsConstructor
public class RankingApi {

    private final UnivRankingRepository univRankingRepository;

    @GetMapping
    public List<UnivRankingResponse> getUnivRanking(
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return univRankingRepository.getUnivRanking(page);
    }

    @GetMapping("/{univName}")
    public List<UnivStudentRankingResponse> getUnivStudentRanking(
            @PathVariable("univName") String univName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "admissionYear", required = false) Integer admissionYear,
            @RequestParam(value = "mainPosition", required = false) LolPosition mainPosition
    ) {
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(major, admissionYear, mainPosition);
        return univRankingRepository.getUnivStudentRanking(univName, page, univStudentRankingFilter);
    }
}
