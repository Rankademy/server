package maruhxn.rankademy.adapter.webapi.ranking;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.persistence.ranking.TotalRankingRepository;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class TotalRankingApi {

    private final TotalRankingRepository totalRankingRepository;

    @GetMapping("/univ")
    public List<UnivRankingResponse> getUnivRanking(
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return totalRankingRepository.getUnivRanking(page);
    }

    @GetMapping("/users")
    public List<UnivRankingResponse> getTotalUserRanking(
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return totalRankingRepository.getTotalUserRanking(page);
    }
}
