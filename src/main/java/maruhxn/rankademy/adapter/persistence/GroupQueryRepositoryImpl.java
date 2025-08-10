package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.dto.*;
import maruhxn.rankademy.application.group.required.GroupQueryRepository;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.group.QGroupMember;
import maruhxn.rankademy.domain.user.QUser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public List<GroupResponse> getRankingList(int page, String keyword, GroupSortKey sortKey) {
        QGroupMember leaderMember = new QGroupMember("leaderMember");
        QUser leaderUser = new QUser("leaderUser");

        return queryFactory
                .select(
                        Projections.constructor(
                                GroupResponse.class,
                                group.id,
                                group.name,
                                group.logoImage,
                                group.capacity,
                                group.members.size(),
                                Expressions.constant(0),
                                Expressions.constant(0),
                                summonerInfo.tierInfo.mappedTier.avg(),
                                Projections.constructor(
                                        LeaderDto.class,
                                        leaderUser.id,
                                        leaderUser.summonerInfo.summonerName,
                                        leaderUser.summonerInfo.summonerIconNum
                                )
                        )
                )
                .from(group)
                .join(group.members, groupMember)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .join(leaderMember).on(leaderMember.group.id.eq(group.id).and(leaderMember.role.eq(GroupRole.LEADER)))
                .join(leaderMember.user, leaderUser)
                .where(keyword != null ? group.name.like("%" + keyword + "%") : null)
                .groupBy(group.id, group.name, group.logoImage, group.capacity, leaderUser.id, leaderUser.summonerInfo.summonerIconNum)
                .orderBy(this.getOrderSpecifier(sortKey), group.createdAt.asc())
                .offset(page * 20L)
                .limit(20)
                .fetch();
    }

    private OrderSpecifier<?> getOrderSpecifier(GroupSortKey sortKey) {
        return switch (sortKey) {
            case TIER -> summonerInfo.tierInfo.mappedTier.avg().desc();
            case WIN_COUNT -> summonerInfo.winCount.sum().desc(); // TODO: 대항전 승리 순으로 변경 필요
        };
    }

    @Override
    public Optional<GroupDetailResponse> getGroupDetails(Long userId, Long groupId) {
        QGroupMember leaderMember = new QGroupMember("leaderMember");
        QUser leaderUser = new QUser("leaderUser");

        return Optional.ofNullable(queryFactory
                .select(
                        Projections.constructor(
                                GroupDetailResponse.class,
                                group.id,
                                group.name,
                                group.about,
                                group.logoImage,
                                summonerInfo.tierInfo.mappedTier.avg(),
                                Expressions.nullExpression(RecordInfoDto.class),
                                group.capacity.longValue(),
                                group.members.size().longValue(),
                                Projections.constructor(
                                        LeaderDto.class,
                                        leaderUser.id,
                                        leaderUser.summonerInfo.summonerName,
                                        leaderUser.summonerInfo.summonerIconNum
                                ),
                                group.createdAt,
                                userId == null
                                        ? Expressions.constant(false)
                                        : JPAExpressions.selectOne()
                                        .from(groupMember)
                                        .where(groupMember.group.id.eq(groupId)
                                                .and(groupMember.user.id.eq(userId)))
                                        .exists()
                        )
                )
                .from(group)
                .join(group.members, groupMember)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .join(leaderMember).on(leaderMember.group.id.eq(group.id).and(leaderMember.role.eq(GroupRole.LEADER)))
                .join(leaderMember.user, leaderUser)
                .where(group.id.eq(groupId))
                .groupBy(group.id, group.name, group.about, group.logoImage, group.capacity, group.createdAt, leaderUser.id, leaderUser.summonerInfo.summonerIconNum)
                .fetchOne());
    }

    @Override
    public List<RecruitmentPostResponse> getRecruitmentPostList(int page) {
        return queryFactory
                .select(
                        Projections.constructor(
                                RecruitmentPostResponse.class,
                                groupRecruitmentPost.id,
                                group.id,
                                group.name,
                                groupRecruitmentPost.title,
                                groupRecruitmentPost.content,
                                groupRecruitmentPost.createdAt
                        )
                )
                .from(groupRecruitmentPost)
                .join(group).on(group.recruitmentPost.id.eq(groupRecruitmentPost.id).and(group.isRecruiting.eq(true)))
                .where(groupRecruitmentPost.isActive.eq(true))
                .orderBy(groupRecruitmentPost.lastUppedAt.desc(), groupRecruitmentPost.createdAt.desc())
                .offset(page * 10L)
                .limit(10)
                .fetch();
    }

    @Override
    public Optional<RecruitmentPostDetailResponse> getRecruitmentPostDetail(Long userId, Long groupId) {
        return Optional.ofNullable(queryFactory
                .select(
                        Projections.constructor(
                                RecruitmentPostDetailResponse.class,
                                groupRecruitmentPost.id,
                                groupRecruitmentPost.title,
                                groupRecruitmentPost.content,
                                groupRecruitmentPost.requirements,
                                groupRecruitmentPost.createdAt,
                                JPAExpressions.selectOne()
                                        .from(groupMember)
                                        .where(groupMember.group.id.eq(groupId).and(groupMember.user.id.eq(userId)))
                                        .exists()
                        )
                )
                .from(group)
                .join(group.recruitmentPost, groupRecruitmentPost)
                .where(group.id.eq(groupId).and(group.recruitmentPost.isActive.eq(true)))
                .fetchOne());
    }

    @Override
    public List<GroupMemberResponse> getGroupMembers(Long groupId, int page) {
        return queryFactory
                .select(
                        Projections.constructor(
                                GroupMemberResponse.class,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                summonerInfo.summonerIconNum,
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
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .where(groupMember.group.id.eq(groupId))
                .offset(page * 7L)
                .limit(7)
                .fetch();
    }

    @Override
    public List<JoinRequestResponse> getJoinRequestList(Long groupId, int page) {
        return queryFactory
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
                .offset(page * 10L)
                .limit(10)
                .fetch();
    }
}

