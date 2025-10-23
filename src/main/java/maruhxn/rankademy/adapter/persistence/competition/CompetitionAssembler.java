package maruhxn.rankademy.adapter.persistence.competition;

import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.SetResultResponse;
import maruhxn.rankademy.application.competition.provided.dto.TeamInfoResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class CompetitionAssembler {

    public List<CompetitionPageResponse> assemble(
            List<CompetitionPageRowReader.Row> rows,
            Map<Long, TeamInfoResponse> teamInfo,
            Map<Long, List<SetResultResponse>> setMap
    ) {
        var items = rows.stream()
                .map(r ->
                        new CompetitionPageResponse(
                                r.cid(),
                                r.otherUniv(),
                                r.status(),
                                must(teamInfo, r.myTid()),
                                must(teamInfo, r.otherTid()),
                                r.submittedAt(), r.isWin(),
                                setMap.getOrDefault(r.cid(), List.of())
                        )
                ).toList();

        return items;
    }

    public static TeamInfoResponse must(Map<Long, TeamInfoResponse> map, Long teamId) {
        var v = map.get(teamId);
        if (v == null) throw new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + teamId);
        return v;
    }
}
