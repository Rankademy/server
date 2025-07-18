package maruhxn.rankademy.domain.member;

import maruhxn.rankademy.domain.member.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.service.SummonerInfoConnector;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class MemberFixture {

    public static MemberRegisterRequest createMemberRegisterRequest(String email) {
        return new MemberRegisterRequest(email, "maruhxn", "verysecret");
    }

    public static MemberRegisterRequest createMemberRegisterRequest() {
        return createMemberRegisterRequest("maruhxn@rankademy.app");
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
        return MemberFixture::createSummonerInfo;
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

    public static Member createMember() {
        return Member.register(createMemberRegisterRequest(), createPasswordEncoder());
    }

    public static Member createMember(Long id) {
        Member member = Member.register(createMemberRegisterRequest(), createPasswordEncoder());
        ReflectionTestUtils.setField(member, "id", id);
        return member;
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
}