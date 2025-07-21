package maruhxn.rankademy.domain.user;

import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import maruhxn.rankademy.domain.user.service.UserTitleProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

public class UserFixture {

    public static UserRegisterRequest createUserRegisterRequest(String email) {
        return new UserRegisterRequest(email, "maruhxn", "verysecret");
    }

    public static UserRegisterRequest createUserRegisterRequest() {
        return createUserRegisterRequest("maruhxn@rankademy.app");
    }

    public static PasswordEncoder createPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(String password) {
                return password.toUpperCase();
            }

            @Override
            public boolean matches(String password, String passwordHash) {
                return encode(password).equals(passwordHash);
            }
        };
    }

    public static SummonerInfoConnector createSummonerInfoConnector() {
        return UserFixture::createSummonerInfo;
    }

    public static SummonerInfo createSummonerInfo(RiotAuthRequest request) {
        return new SummonerInfo(
                "MfiVjqqTLQ_XhERTcyHydIdiFmlQhK9zNTfKSel_DECSZHGgTIITI7QmHGGaPDbpjlPVOqAahCtHzA",
                request.summonerName(),
                request.summonerTag(),
                12345,
                new TierInfo("BRONZE", "II", 50),
                100,
                50.0,
                LocalDateTime.now()
        );
    }

    public static User createUser() {
        return User.register(createUserRegisterRequest(), createPasswordEncoder());
    }

    public static User createUser(Long id) {
        User user = User.register(createUserRegisterRequest(), createPasswordEncoder());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static EnrollUnivRequest createEnrollUnivRequest() {
        return createEnrollUnivRequest("서울과학기술대학교", "test@seoultech.ac.kr");
    }

    public static EnrollUnivRequest createEnrollUnivRequest(String univname, String univMail) {
        return new EnrollUnivRequest(
                univname,
                univMail,
                true,
                2020,
                "컴퓨터공학과"
        );
    }

    public static RiotAuthRequest createRiotAuthRequest() {
        return new RiotAuthRequest("maruhxn", "KOR");
    }

    public static UserTitleProvider createTitleProvider() {
        return userId -> List.of("DUMMY");
    }
}