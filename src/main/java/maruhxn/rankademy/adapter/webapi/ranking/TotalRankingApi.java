package maruhxn.rankademy.adapter.webapi.ranking;

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
public class TotalRankingApi {

    private final TotalRankingRepository totalRankingRepository;

    @GetMapping("/univ")
    public List<UnivRankingResponse> getUnivRanking(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "univNameKey", required = false) String univNameKey
    ) {
        return totalRankingRepository.getUnivRanking(page, univNameKey);
    }

    @GetMapping("/users")
    public PagedModel<TotalUserRankingResponse> getTotalUserRanking(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "userNameKey", required = false) String userNameKey
    ) {
        return totalRankingRepository.getTotalUserRanking(page, userNameKey);
    }
}
