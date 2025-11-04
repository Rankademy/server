package maruhxn.rankademy.adapter.persistence.competition;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competition.provided.dto.SetResultResponse;
import maruhxn.rankademy.application.competition.provided.dto.TeamInfoResponse;
import maruhxn.rankademy.domain.competition.QSetResult;
import maruhxn.rankademy.domain.group.QGroup;
import maruhxn.rankademy.domain.team.QTeam;
import maruhxn.rankademy.domain.team.QTeamMember;
import maruhxn.rankademy.domain.user.QSummonerInfo;
import maruhxn.rankademy.domain.user.QUser;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeamAndSetLoader {

    private final JPAQueryFactory queryFactory;

    public Map<Long, TeamInfoResponse> loadTeams(Set<Long> teamIds) {
        QTeam t = QTeam.team;
        QGroup g = QGroup.group;
        QTeamMember tm = QTeamMember.teamMember;
        QUser u = QUser.user;
        QSummonerInfo si = QSummonerInfo.summonerInfo;

        var rows = queryFactory.select(t.id, t.name, g.logoImage, g.name, u.id, tm.position, si.summonerName, si.summonerTag)
                .from(t).join(g).on(t.groupId.eq(g.id))
                .join(tm).on(tm.team.id.eq(t.id))
                .join(u).on(tm.user.id.eq(u.id))
                .join(si).on(si.id.eq(u.summonerInfo.id))
                .where(t.id.in(teamIds))
                .fetch();

        return rows.stream().collect(Collectors.groupingBy(
                r -> r.get(t.id),
                LinkedHashMap::new,
                Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Tuple row = list.get(0);
                    Long id = row.get(t.id);
                    String tName = row.get(t.name);
                    String gLogo = row.get(g.logoImage);
                    String gName = row.get(g.name);
                    var members = list.stream()
                            .map(r -> new TeamInfoResponse.TeamMemberResponse(
                                    r.get(u.id),
                                    r.get(tm.position),
                                    r.get(si.summonerName),
                                    r.get(si.summonerTag),
                                    r.get(si.summonerIcon)
                                    ))
                            .toList();
                    return new TeamInfoResponse(id, tName, gLogo, gName, members);
                })
        ));
    }

    public Map<Long, List<SetResultResponse>> loadSetResults(List<Long> competitionIds) {
        QSetResult sr = QSetResult.setResult;
        return queryFactory.select(sr.competition.id, sr.setNumber, sr.winnerTeamId)
                .from(sr)
                .where(sr.competition.id.in(competitionIds))
                .orderBy(sr.competition.id.asc(), sr.setNumber.asc())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.get(sr.competition.id),
                        LinkedHashMap::new,
                        Collectors.mapping(r -> new SetResultResponse(
                                r.get(sr.setNumber), r.get(sr.winnerTeamId)), Collectors.toList())));
    }
}
