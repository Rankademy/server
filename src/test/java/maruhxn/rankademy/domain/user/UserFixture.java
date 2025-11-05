package maruhxn.rankademy.domain.user;

import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import maruhxn.rankademy.domain.user.service.UserTitleProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static maruhxn.rankademy.domain.user.OAuth2Provider.GOOGLE;

public class UserFixture {

    public static UserOAuth2CreateRequest createUserOAuth2CreateRequest(String email, String username) {
        return new UserOAuth2CreateRequest(email, username, GOOGLE, UUID.randomUUID().toString());
    }

    public static UserOAuth2CreateRequest createUserOAuth2CreateRequest(String email) {
        return createUserOAuth2CreateRequest(email, "maruhxn");
    }

    public static UserOAuth2CreateRequest createUserOAuth2CreateRequest() {
        return createUserOAuth2CreateRequest("maruhxn@rankademy.app");
    }

    public static SummonerInfoConnector createSummonerInfoConnector() {
        return UserFixture::createSummonerInfo;
    }


    public static SummonerInfoConnector createSummonerInfoConnector(String puuid) {
        return request -> createSummonerInfo(request, puuid);
    }


    public static SummonerInfoConnector createSummonerInfoConnector(TierInfo tierInfo) {
        return request -> createSummonerInfo(request, tierInfo);
    }

    public static SummonerInfoConnector createSummonerInfoConnector(String puuid, TierInfo tierInfo) {
        return request -> createSummonerInfo(request, puuid, tierInfo);
    }

    public static SummonerInfo createSummonerInfo(RiotAuthRequest request) {
        return new SummonerInfo(
                UUID.randomUUID().toString(),
                request.summonerName(),
                request.summonerTag(),
                12345,
                new TierInfo(Tier.BRONZE, Rank.II, 50),
                100,
                100,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static SummonerInfo createSummonerInfo(RiotAuthRequest request, String puuid) {
        return new SummonerInfo(
                puuid,
                request.summonerName(),
                request.summonerTag(),
                12345,
                new TierInfo(Tier.BRONZE, Rank.II, 50),
                100,
                100,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static SummonerInfo createSummonerInfo(RiotAuthRequest request, TierInfo tierInfo) {
        return new SummonerInfo(
                UUID.randomUUID().toString(),
                request.summonerName(),
                request.summonerTag(),
                12345,
                tierInfo,
                100,
                100,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static SummonerInfo createSummonerInfo(RiotAuthRequest request, String puuid, TierInfo tierInfo) {
        return new SummonerInfo(
                puuid,
                request.summonerName(),
                request.summonerTag(),
                12345,
                tierInfo,
                100,
                100,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static User createUser() {
        return new User("maruhxn", new Email("maruhxn@rankademy.app"));
    }

    public static User createUser(String email) {
        return new User("maruhxn", new Email(email));
    }

    public static User createUser(String email, String username) {
        return new User(username, new Email(email));
    }

    public static User createUser(Long id) {
        User user = createUser();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static EnrollUnivRequest createEnrollUnivRequest() {
        return createEnrollUnivRequest("서울과학기술대학교", "test@seoultech.ac.kr");
    }

    public static EnrollUnivRequest createEnrollUnivRequest(String univname, String univMail) {
        return new EnrollUnivRequest(univname, univMail);
    }

    public static RiotAuthRequest createRiotAuthRequest() {
        return new RiotAuthRequest("maruhxn", "KOR");
    }

    public static RiotAuthRequest createRiotAuthRequest(String summonerName, String summonerTag) {
        return new RiotAuthRequest(summonerName, summonerTag);
    }

    public static UserTitleProvider createTitleProvider() {
        return userId -> List.of("DUMMY");
    }

    public static User createAuthorizedMember(String email, String username) {
        User member = createUser(email, username);
        member.completeUnivAuthentication(createEnrollUnivRequest("서울과학기술대학교", username + "@seoultech.ac.kr"));
        member.connectSummonerInfo(createSummonerInfoConnector(username + "-puuid"), createRiotAuthRequest(username, "KR1"));
        return member;
    }
}
