package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.dto.*;
import maruhxn.rankademy.application.group.required.GroupQueryRepository;
import maruhxn.rankademy.domain.competition.QCompetition;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.group.QGroup;
import maruhxn.rankademy.domain.group.QGroupMember;
import maruhxn.rankademy.domain.team.QTeam;
import maruhxn.rankademy.domain.user.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static maruhxn.rankademy.adapter.persistence.ranking.WhereClauseHelper.searchBySummonerNameKey;
import static maruhxn.rankademy.domain.group.QGroup.group;
import static maruhxn.rankademy.domain.group.QGroupMember.groupMember;
import static maruhxn.rankademy.domain.group.QGroupRecruitmentPost.groupRecruitmentPost;
import static maruhxn.rankademy.domain.group.QJoinRequest.joinRequest;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class GroupQueryRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MyGroupResponse> getMyGroupList(Long userId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                MyGroupResponse.class,
                                group.id,
                                group.name,
                                group.logoImage,
                                group.about,
                                group.createdAt
                        )
                )
                .from(groupMember)
                .join(groupMember.group, group)
                .where(groupMember.user.id.eq(userId))
                .fetch();
    }

    @Override
    public List<RecentCompetitionResponse> getRecentCompetitions(Long groupId) {
        QCompetition competition = QCompetition.competition;
        QTeam teamA = new QTeam("teamA");
        QTeam teamB = new QTeam("teamB");
        QGroup groupA = new QGroup("groupA");
        QGroup groupB = new QGroup("groupB");

        StringExpression opponentGroupName = new CaseBuilder()
                .when(teamA.groupId.eq(groupId)).then(groupB.name)
                .otherwise(groupA.name);

        NumberExpression<Long> opponentGroupId = new CaseBuilder()
                .when(teamA.groupId.eq(groupId)).then(groupB.id)
                .otherwise(groupA.id);

        BooleanExpression isWin = new CaseBuilder()
                .when(competition.finalWinnerGroupId.eq(groupId)).then(true)
                .otherwise(false);

        return queryFactory
                .select(Projections.constructor(
                        RecentCompetitionResponse.class,
                        competition.id,
                        opponentGroupId,
                        opponentGroupName,
                        isWin,
                        competition.status
                ))
                .from(competition)
                .join(teamA).on(teamA.id.eq(competition.team1Id))
                .join(groupA).on(teamA.groupId.eq(groupA.id))
                .join(teamB).on(teamB.id.eq(competition.team2Id))
                .join(groupB).on(teamB.groupId.eq(groupB.id))
                .where(teamA.groupId.eq(groupId).or(teamB.groupId.eq(groupId)))
                .orderBy(competition.id.desc())
                .limit(3)
                .fetch();
    }

    @Override
    public List<MyGroupSummaryResponse> getMyGroupSummaries(Long userId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                MyGroupSummaryResponse.class,
                                group.id,
                                group.name
                        )
                )
                .from(groupMember)
                .join(groupMember.group, group)
                .where(groupMember.user.id.eq(userId))
                .fetch();
    }

    @Override
    public List<SearchGroupMemberResponse> searchGroupMembers(Long groupId, String memberNameKey) {
        return queryFactory
                .select(
                        Projections.constructor(
                                SearchGroupMemberResponse.class,
                                user.id,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                summonerInfo.summonerIcon
                        )
                )
                .from(groupMember)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .where(
                        groupMember.group.id.eq(groupId),
                        searchBySummonerNameKey(memberNameKey)
                )
                .limit(4)
                .fetch();
    }

    @Override
    public Optional<GroupDetailResponse> getGroupDetails(Long userId, Long groupId) {
        QGroupMember leaderMember = new QGroupMember("leaderMember");
        QUser leaderUser = new QUser("leaderUser");

        var avgMappedTierExpr = summonerInfo.tierInfo.mappedTier.avg();
        var capacityExpr = group.capacity.longValue();
        var memberCountExpr = group.members.size().longValue();

        Tuple head = queryFactory
                .select(
                        group.id,
                        group.name,
                        group.about,
                        group.logoImage,
                        avgMappedTierExpr,
                        capacityExpr,
                        memberCountExpr,
                        leaderUser.id,
                        leaderUser.summonerInfo.summonerName,
                        leaderUser.summonerInfo.summonerTag,
                        leaderUser.summonerInfo.summonerIcon,
                        group.createdAt,
                        group.isRecruiting,
                        group.univName
                )
                .from(group)
                .join(group.members, groupMember)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .join(leaderMember).on(leaderMember.group.id.eq(group.id).and(leaderMember.role.eq(GroupRole.LEADER)))
                .join(leaderMember.user, leaderUser)
                .where(group.id.eq(groupId))
                .groupBy(
                        group.id,
                        group.name,
                        group.about,
                        group.logoImage,
                        group.capacity,
                        leaderUser.id,
                        leaderUser.summonerInfo.summonerName,
                        leaderUser.summonerInfo.summonerIcon,
                        group.createdAt
                )
                .fetchOne();

        if (head == null) {
            return Optional.empty();
        }

        boolean isJoined = false;
        boolean isLeader = false;

        if (userId != null) {
            isJoined = queryFactory
                    .selectOne()
                    .from(groupMember)
                    .where(groupMember.group.id.eq(groupId)
                            .and(groupMember.user.id.eq(userId)))
                    .fetchFirst() != null;

            Long leaderId = head.get(leaderUser.id);
            isLeader = Objects.equals(leaderId, userId);
        }

        Double avgMappedTier = head.get(avgMappedTierExpr);
        Long capacity = head.get(capacityExpr);
        Long memberCount = head.get(memberCountExpr);
        LeaderDto leaderDto = new LeaderDto(
                head.get(leaderUser.id),
                head.get(leaderUser.summonerInfo.summonerName),
                head.get(leaderUser.summonerInfo.summonerTag),
                head.get(leaderUser.summonerInfo.summonerIcon)
        );

        return Optional.of(new GroupDetailResponse(
                head.get(group.id),
                head.get(group.name),
                head.get(group.about),
                head.get(group.logoImage),
                avgMappedTier != null ? avgMappedTier : 0.0,
                null,
                capacity != null ? capacity : 0L,
                memberCount != null ? memberCount : 0L,
                leaderDto,
                head.get(group.createdAt),
                isJoined,
                isLeader,
                head.get(group.isRecruiting),
                head.get(group.univName)
        ));
    }

    @Override
    public Page<RecruitmentPostResponse> getRecruitmentPostList(int page) {
        Pageable pageable = PageRequest.of(page, 10);

        Long total = queryFactory.select(groupRecruitmentPost.count())
                .from(groupRecruitmentPost)
                .join(group).on(group.recruitmentPost.id.eq(groupRecruitmentPost.id).and(group.isRecruiting.eq(true)))
                .where(groupRecruitmentPost.isActive.eq(true))
                .fetchOne();

        if(total == null || total <= 0L) return new PageImpl<>(List.of(), pageable, 0L);

        List<RecruitmentPostResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                RecruitmentPostResponse.class,
                                groupRecruitmentPost.id,
                                group.id,
                                group.name,
                                group.logoImage,
                                groupRecruitmentPost.title,
                                groupRecruitmentPost.content,
                                groupRecruitmentPost.createdAt
                        )
                )
                .from(groupRecruitmentPost)
                .join(group).on(group.recruitmentPost.id.eq(groupRecruitmentPost.id).and(group.isRecruiting.eq(true)))
                .where(groupRecruitmentPost.isActive.eq(true))
                .orderBy(groupRecruitmentPost.lastUppedAt.desc(), groupRecruitmentPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Optional<RecruitmentPostDetailResponse> getRecruitmentPostDetail(Long userId, Long groupId) {
        return Optional.ofNullable(queryFactory
                .select(
                        Projections.constructor(
                                RecruitmentPostDetailResponse.class,
                                groupRecruitmentPost.id,
                                group.id,
                                group.name,
                                groupRecruitmentPost.title,
                                groupRecruitmentPost.content,
                                groupRecruitmentPost.requirements,
                                groupRecruitmentPost.createdAt,
                                JPAExpressions.selectOne()
                                        .from(groupMember)
                                        .where(groupMember.group.id.eq(groupId).and(groupMember.user.id.eq(userId)))
                                        .exists(),
                                group.leader.id.eq(userId)
                        )
                )
                .from(group)
                .join(group.recruitmentPost, groupRecruitmentPost)
                .where(group.id.eq(groupId).and(group.recruitmentPost.isActive.eq(true)))
                .fetchOne());
    }

    @Override
    public Page<GroupMemberResponse> getGroupMembers(Long groupId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 20);

        Long total = queryFactory
                .select(groupMember.count())
                .from(groupMember)
                .join(groupMember.group, group)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .where(groupMember.group.id.eq(groupId))
                .fetchOne();

        if(total == null || total <= 0L) return new PageImpl(List.of(), pageRequest, total);

        List<GroupMemberResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                GroupMemberResponse.class,
                                user.id,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                summonerInfo.summonerIcon,
                                user.univInfo.major,
                                user.univInfo.admissionYear,
                                user.mainPosition,
                                user.subPosition,
                                summonerInfo.tierInfo,
                                Projections.constructor(
                                        RecordInfoDto.class,
                                        summonerInfo.winCount,
                                        summonerInfo.lossCount
                                )
                        )
                )
                .from(groupMember)
                .join(groupMember.group, group)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .where(groupMember.group.id.eq(groupId))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        return new PageImpl(result, pageRequest, total);
    }

    @Override
    public Page<GroupMemberResponse> getGroupMembersWithoutLeader(Long groupId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 20);

        Long total = queryFactory
                .select(groupMember.count())
                .from(groupMember)
                .join(groupMember.group, group)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .where(groupMember.group.id.eq(groupId), groupMember.user.id.ne(group.leader.id))
                .fetchOne();

        if(total == null || total <= 0L) return new PageImpl(List.of(), pageRequest, total);

        List<GroupMemberResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                GroupMemberResponse.class,
                                user.id,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                summonerInfo.summonerIcon,
                                user.univInfo.major,
                                user.univInfo.admissionYear,
                                user.mainPosition,
                                user.subPosition,
                                summonerInfo.tierInfo,
                                Projections.constructor(
                                        RecordInfoDto.class,
                                        summonerInfo.winCount,
                                        summonerInfo.lossCount
                                )
                        )
                )
                .from(groupMember)
                .join(groupMember.group, group)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .where(groupMember.group.id.eq(groupId), groupMember.user.id.ne(group.leader.id))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        return new PageImpl(result, pageRequest, total);
    }

    @Override
    public Page<JoinRequestResponse> getJoinRequestList(Long groupId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 10);

        Long total = queryFactory
                .select(group.count())
                .from(group)
                .join(group.joinRequests, joinRequest)
                .join(user).on(user.id.eq(joinRequest.userId))
                .join(user.summonerInfo, summonerInfo)
                .where(group.id.eq(groupId))
                .fetchOne();

        if(total == null || total <= 0L) return new PageImpl<>(List.of(), pageRequest, 0L);

        List<JoinRequestResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                JoinRequestResponse.class,
                                user.id,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                joinRequest.requestedAt
                        )
                )
                .from(group)
                .join(group.joinRequests, joinRequest)
                .join(user).on(user.id.eq(joinRequest.userId))
                .join(user.summonerInfo, summonerInfo)
                .where(group.id.eq(groupId))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        return new PageImpl(result, pageRequest, total);
    }
}
