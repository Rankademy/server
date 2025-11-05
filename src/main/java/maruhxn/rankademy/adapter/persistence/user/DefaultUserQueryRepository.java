package maruhxn.rankademy.adapter.persistence.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.SearchedUserResponse;
import maruhxn.rankademy.application.user.dto.MyProfileResponse;
import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.application.user.required.UserQueryRepository;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.UserAuthStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static maruhxn.rankademy.adapter.persistence.ranking.WhereClauseHelper.searchBySummonerNameKey;
import static maruhxn.rankademy.domain.user.QChampionPlayRecord.championPlayRecord;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class DefaultUserQueryRepository implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProfileResponse> getProfile(Long userId) {
        ProfileBase base = queryFactory
                .select(Projections.constructor(
                        ProfileBase.class,
                        user.id,
                        Projections.constructor(
                                ProfileResponse.SummonerInfoResponse.class,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                summonerInfo.summonerIcon,
                                summonerInfo.tierInfo,
                                summonerInfo.winCount,
                                summonerInfo.lossCount
                        ),
                        Projections.constructor(
                                ProfileResponse.UnivInfoResponse.class,
                                user.univInfo.univName,
                                user.univInfo.major,
                                user.univInfo.admissionYear
                        ),
                        user.description,
                        user.mainPosition,
                        user.subPosition,
                        summonerInfo.tierInfo,
                        summonerInfo.winCount,
                        summonerInfo.lossCount,
                        summonerInfo.id
                ))
                .from(user)
                .join(summonerInfo).on(user.summonerInfo.id.eq(summonerInfo.id))
                .where(
                        user.id.eq(userId)
                                .and(user.authStatus.eq(UserAuthStatus.AUTHORIZED))
                )
                .fetchOne();

        if (base == null) return Optional.empty();

        List<String> mostChampions = queryFactory
                .select(championPlayRecord.championId)
                .from(summonerInfo)
                .join(summonerInfo.mostChampions, championPlayRecord)
                .where(summonerInfo.id.eq(base.summonerInfoId))
                .fetch();

        return Optional.of(new ProfileResponse(
                base.id(),
                base.summonerInfo(),
                base.univInfo(),
                base.description(),
                mostChampions,
                base.mainPosition(),
                base.subPosition()
        ));
    }

    @Override
    public Optional<MyProfileResponse> getMyProfile(Long userId) {
        MyProfileBase base = queryFactory
                .select(Projections.constructor(
                        MyProfileBase.class,
                        user.id,
                        user.username,
                        Projections.constructor(
                                MyProfileResponse.SummonerInfoResponse.class,
                                summonerInfo.puuid,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                summonerInfo.summonerIcon,
                                summonerInfo.tierInfo,
                                summonerInfo.winCount,
                                summonerInfo.lossCount
                        ),
                        Projections.constructor(
                                MyProfileResponse.UnivInfoResponse.class,
                                user.univInfo.univName,
                                user.univInfo.univMail.address,
                                user.univInfo.major,
                                user.univInfo.admissionYear
                        ),
                        user.description,
                        user.mainPosition,
                        user.subPosition,
                        summonerInfo.id
                ))
                .from(user)
                .leftJoin(summonerInfo).on(user.summonerInfo.id.eq(summonerInfo.id))
                .where(user.id.eq(userId))
                .fetchOne();

        if (base == null) return Optional.empty();

        List<String> mostChampions = new ArrayList<>();
        if (base.summonerInfoId != null) {
            mostChampions = queryFactory
                    .select(championPlayRecord.championId)
                    .from(summonerInfo)
                    .join(summonerInfo.mostChampions, championPlayRecord)
                    .where(summonerInfo.id.eq(base.summonerInfoId))
                    .fetch();
        }

        return Optional.of(new MyProfileResponse(
                base.id(),
                base.username(),
                base.summonerInfo(),
                base.univInfo(),
                base.description(),
                mostChampions,
                base.mainPosition(),
                base.subPosition()
        ));
    }

    @Override
    public List<SearchedUserResponse> searchUsersByKey(String userNameKey) {
        return queryFactory
                .select(
                        Projections.constructor(
                                SearchedUserResponse.class,
                                user.id,
                                summonerInfo.summonerName,
                                summonerInfo.summonerTag,
                                summonerInfo.summonerIcon
                        )
                )
                .from(user)
                .join(user.summonerInfo, summonerInfo)
                .where(searchBySummonerNameKey(userNameKey))
                .limit(4)
                .fetch();
    }

    /**
     * 내부 조립용 베이스 DTO (레코드 예시)
     */
    public record ProfileBase(
            Long id,
            ProfileResponse.SummonerInfoResponse summonerInfo,
            ProfileResponse.UnivInfoResponse univInfo,
            String description,
            LolPosition mainPosition,
            LolPosition subPosition,
            TierInfo tierInfo,
            Integer winCount,
            Integer lossCount,
            Long summonerInfoId
    ) {
    }

    public record MyProfileBase(
            Long id,
            String username,
            MyProfileResponse.SummonerInfoResponse summonerInfo,
            MyProfileResponse.UnivInfoResponse univInfo,
            String description,
            LolPosition mainPosition,
            LolPosition subPosition,
            Long summonerInfoId
    ) {
    }
}
