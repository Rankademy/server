package maruhxn.rankademy.application.competition.required;

import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;

public interface CompetitionQueryRepository {

    CompetitionResultResponse getResult(Long id); // 대항전 결과 조회

}
