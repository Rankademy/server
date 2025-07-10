package maruhxn.rankademy.domain.member;

import maruhxn.rankademy.domain.member.dto.CertifySummonerInfoRequest;
import maruhxn.rankademy.domain.member.dto.CertifyUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
import org.springframework.test.util.ReflectionTestUtils;

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

    public static Member createMember(Long id) {
        Member member = Member.register(createMemberRegisterRequest(), createPasswordEncoder());
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public static CertifyUnivRequest createCertifyUnivRequest() {
        return new CertifyUnivRequest(
                "서울과학기술대학교",
                "test@seoultech.ac.kr",
                true,
                true,
                2020,
                "컴퓨터공학과"
        );
    }

    public static CertifySummonerInfoRequest createCertifySummonerInfoRequest() {
        return new CertifySummonerInfoRequest(
                "puuid",
                "maruhxn",
                "KOR",
                1234,
                "CHALLENGER",
                "I",
                100,
                0.55
        );
    }
}