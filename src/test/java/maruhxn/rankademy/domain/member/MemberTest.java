package maruhxn.rankademy.domain.member;

import maruhxn.rankademy.domain.member.dto.CertifySummonerInfoRequest;
import maruhxn.rankademy.domain.member.dto.CertifyUnivRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static maruhxn.rankademy.domain.member.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    Member member;
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        this.passwordEncoder = createPasswordEncoder();
        member = Member.register(
                createMemberRegisterRequest(),
                passwordEncoder
        );
    }

    @Test
    @DisplayName("정상적으로 회원가입을 한다")
    void registerMember() {
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("유효하지 않은 이메일 형식으로 회원가입 시 예외가 발생한다")
    void registerFailByInvalidEmail() {
        assertThatThrownBy(() ->
                Member.register(createMemberRegisterRequest("invalid email"), passwordEncoder)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호를 올바르게 검증한다")
    void verifyPassword() {
        assertThat(member.verifyPassword("verysecret", passwordEncoder)).isTrue();
        assertThat(member.verifyPassword("hello", passwordEncoder)).isFalse();
    }

    @Test
    @DisplayName("비밀번호를 변경한다")
    void changePassword() {
        member.changePassword("verysecret2", passwordEncoder);
        assertThat(member.verifyPassword("verysecret2", passwordEncoder)).isTrue();
    }

    @Test
    @DisplayName("학교 및 라이엇 계정 인증 완료 시 최종 인증에 성공한다")
    void isAuthorized_Success() {
        // given
        member.completeUniversityAuthentication(createCertifyUnivRequest());
        member.completeRiotAuthentication(createCertifySummonerInfoRequest());

        // when
        boolean isAuthorized = member.isAuthorized();

        // then
        assertThat(isAuthorized).isTrue();
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("학교 인증만 했을 경우 최종 인증에 실패한다")
    void isAuthorized_Fail_When_UnivOnly() {
        // given
        member.completeUniversityAuthentication(createCertifyUnivRequest());

        // when & then
        assertThatThrownBy(() -> member.isAuthorized())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("소환사 정보를 등록해주세요");
    }

    @Test
    @DisplayName("라이엇 계정 인증만 했을 경우 최종 인증에 실패한다")
    void isAuthorized_Fail_When_RiotOnly() {
        // given
        member.completeRiotAuthentication(createCertifySummonerInfoRequest());

        // when & then
        assertThatThrownBy(() -> member.isAuthorized())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("학교 인증을 완료해주세요");
    }

    @Test
    @DisplayName("학교 정보를 정상적으로 인증(업데이트)한다")
    void completeUniversityAuthentication() {
        // given
        CertifyUnivRequest request = createCertifyUnivRequest();

        // when
        member.completeUniversityAuthentication(request);

        // then
        assertThat(member.getUnivInfo()).isNotNull();
        assertThat(member.getUnivInfo().univName()).isEqualTo(request.univName());
    }

    @Test
    @DisplayName("학교 인증 정보를 제거한다")
    void removeUniversityAuthentication() {
        // given
        member.completeUniversityAuthentication(createCertifyUnivRequest());
        assertThat(member.getUnivInfo()).isNotNull();

        // when
        member.removeUniversityAuthentication();

        // then
        assertThat(member.getUnivInfo()).isNull();
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("라이엇 소환사 정보를 정상적으로 인증(업데이트)한다")
    void completeRiotAuthentication() {
        // given
        CertifySummonerInfoRequest request = createCertifySummonerInfoRequest();

        // when
        member.completeRiotAuthentication(request);

        // then
        assertThat(member.getSummonerInfo()).isNotNull();
        assertThat(member.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
    }

    @Test
    @DisplayName("라이엇 소환사 인증 정보를 제거한다")
    void removeRiotAuthentication() {
        // given
        member.completeRiotAuthentication(createCertifySummonerInfoRequest());
        assertThat(member.getSummonerInfo()).isNotNull();

        // when
        member.removeRiotAuthentication();

        // then
        assertThat(member.getSummonerInfo()).isNull();
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("이미 최종 인증된 멤버가 다시 인증을 시도하면 예외가 발생한다")
    void reAuthentication_Fail() {
        // given
        member.completeUniversityAuthentication(createCertifyUnivRequest());
        member.completeRiotAuthentication(createCertifySummonerInfoRequest());
        member.isAuthorized(); // 최종 인증 완료
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.AUTHORIZED);

        // when & then
        assertThatThrownBy(() -> member.completeUniversityAuthentication(createCertifyUnivRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 인증처리가 완료되었습니다.");

        assertThatThrownBy(() -> member.completeRiotAuthentication(createCertifySummonerInfoRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 인증처리가 완료되었습니다.");
    }
}