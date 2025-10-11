package maruhxn.rankademy.application.competitionrequest;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competitionrequest.provided.CompetitionRequestReader;
import maruhxn.rankademy.application.competitionrequest.required.CompetitionRequestQueryRepository;
import maruhxn.rankademy.application.competitionrequest.required.dto.CompetitionRequestPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompetitionRequestQueryService implements CompetitionRequestReader {

    private final CompetitionRequestQueryRepository competitionRequestQueryRepository;

    @Override
    public CompetitionRequestPageResponse getRequests(Long teamId, int page) {
        return competitionRequestQueryRepository.findAll(teamId, page);
    }

}
