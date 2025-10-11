package maruhxn.rankademy.adapter.persistence.competition;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static maruhxn.rankademy.domain.competition.QCompetition.competition;
import static maruhxn.rankademy.domain.team.QTeam.team;
import static maruhxn.rankademy.domain.team.QTeamMember.teamMember;

@Component
@RequiredArgsConstructor
public class CompetitionCountReader {

    private final JPAQueryFactory queryFactory;

    public Long countForUser(Long userId) {
        return Optional.ofNullable(
                queryFactory.select(competition.id.count())
                        .from(competition)
                        .where(
                                JPAExpressions.selectOne()
                                        .from(teamMember)
                                        .where(teamMember.user.id.eq(userId)
                                                .and(teamMember.team.id.eq(competition.team1Id).or(teamMember.team.id.eq(competition.team2Id))))
                                        .exists()
                        )
                        .fetchOne()
        ).orElse(0L);
    }

    public Long countForGroup(Long groupId) {
        return Optional.ofNullable(
                queryFactory
                        .select(competition.id.count())
                        .from(competition)
                        .where(
                                JPAExpressions.selectOne()
                                        .from(team)
                                        .where(
                                                team.groupId.eq(groupId)
                                                        .and(team.id.eq(competition.team1Id).or(team.id.eq(competition.team2Id)))
                                        )
                                        .exists()
                        )
                        .fetchOne()
        ).orElse(0L);
    }
}
