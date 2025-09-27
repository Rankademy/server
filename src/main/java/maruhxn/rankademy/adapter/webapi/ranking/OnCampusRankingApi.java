package maruhxn.rankademy.adapter.webapi.ranking;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.persistence.ranking.GroupRankingRepository;
import maruhxn.rankademy.adapter.persistence.ranking.OnCampusRankingRepository;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.GroupRankingFilter;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.application.group.provided.dto.GroupResponse;
import maruhxn.rankademy.application.group.provided.dto.GroupSortKey;
import maruhxn.rankademy.domain.user.LolPosition;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rankings/univ/{univName}")
@RequiredArgsConstructor
public class OnCampusRankingApi {

    private final OnCampusRankingRepository onCampusRankingRepository;
    private final GroupRankingRepository groupRankingRepository;

    @GetMapping
    public List<UnivStudentRankingResponse> getUnivStudentRanking(
            @PathVariable("univName") String univName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "admissionYear", required = false) Integer admissionYear,
            @RequestParam(value = "mainPosition", required = false) LolPosition mainPosition
    ) {
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(major, admissionYear, mainPosition);
        return onCampusRankingRepository.getUnivStudentRanking(univName, page, univStudentRankingFilter);
    }

    @GetMapping("/groups")
    public List<GroupResponse> getGroupRankingList(
            @PathVariable("univName") String univName,
            @RequestParam("page") int page,
            @RequestParam(value = "sortKey", required = false) GroupSortKey sortKey,
            @RequestParam(value = "groupNameKey", required = false) String groupNameKey,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "admissionYear", required = false) Integer admissionYear,
            @RequestParam(value = "mainPosition", required = false) LolPosition mainPosition
    ) {
        GroupRankingFilter groupRankingFilter = new GroupRankingFilter(groupNameKey, major, admissionYear, mainPosition);
        return groupRankingRepository.getGroupRanking(univName, page, sortKey, groupRankingFilter);
    }
}
