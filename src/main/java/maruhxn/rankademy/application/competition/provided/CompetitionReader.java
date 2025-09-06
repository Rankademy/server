package maruhxn.rankademy.application.competition.provided;

import maruhxn.rankademy.application.competition.provided.dto.CompetitionDetailResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;

public interface CompetitionReader {

    Boolean checkIsMyCompetition(Long userId, Long competitionId);

    CompetitionDetailResponse getDetail(Long id); // 대항전 상세 정보 조회

    CompetitionResultResponse getResult(Long id); // 대항전 결과 조회

    CompetitionPageResponse getMyCompetitionHistory(Long userId, int page); // 내 대항전 히스토리 페이징 조회

    CompetitionPageResponse getGroupCompetitionHistory(Long groupId, int page); // 그룹의 대항전 히스토리 페이징 조회

}
