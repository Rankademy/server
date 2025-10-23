package maruhxn.rankademy.application.competition.required;

import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;
import org.springframework.data.web.PagedModel;

public interface CompetitionQueryRepository {

    CompetitionResultResponse getResult(Long id); // 대항전 결과 조회

    PagedModel<CompetitionPageResponse> getMyCompetitionHistory(Long userId, int page); // 내 대항전 전적 조회

    PagedModel<CompetitionPageResponse> getGroupCompetitionHistory(Long groupId, int page); // 그룹 대항전 전적 조회
}
