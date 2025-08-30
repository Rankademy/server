package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competitionrequest.required.CompetitionRequestQueryRepository;
import maruhxn.rankademy.application.competitionrequest.required.dto.CompetitionRequestPageResponse;
import maruhxn.rankademy.domain.team.QTeam;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.domain.competitionrequest.QCompetitionRequest.competitionRequest;

@Repository
@RequiredArgsConstructor
public class CompetitionRequestQueryRepositoryImpl implements CompetitionRequestQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CompetitionRequestPageResponse findAll(int page) {
        QTeam fromTeam = QTeam.team;

        List<CompetitionRequestPageResponse.CompetitionRequestResponse> result = queryFactory
                .select(Projections.constructor(
                        CompetitionRequestPageResponse.CompetitionRequestResponse.class,
                        competitionRequest.id,
                        competitionRequest.fromTeamId,
                        fromTeam.name,
                        competitionRequest.requestedAt
                ))
                .from(competitionRequest)
                .join(fromTeam).on(competitionRequest.fromTeamId.eq(fromTeam.id))
                .orderBy(competitionRequest.id.desc())
                .offset(page * 20L)
                .limit(20)
                .fetch();

        Long count = queryFactory
                .select(competitionRequest.count())
                .from(competitionRequest)
                .fetchOne();

        return new CompetitionRequestPageResponse(count, result);
    }
}
