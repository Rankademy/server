package maruhxn.rankademy.application.competition.required;

import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;

public interface CompetitionQueryRepository {

    CompetitionResultResponse getResult(Long id); // 대항전 결과 조회

    CompetitionPageResponse getMyCompetitionHistory(Long userId, int page); // 내 대항전 전적 조회

    CompetitionPageResponse getGroupCompetitionHistory(Long groupId, int page); // 그룹 대항전 전적 조회
}
