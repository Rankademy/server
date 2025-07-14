package maruhxn.rankademy.domain.member;

import maruhxn.rankademy.domain.member.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberProfileUpdateRequest;
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
        member.enrollUnivInfo(createEnrollUnivRequest());
        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        // when /then
        assertThat(member.isAuthorized()).isTrue();
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("최종 인증에 성공 후, 학교 이메일을 수정하면 인증이 해제된다.")
    void updateUnivInfo_Then_Auth_Fail() {
        // given
        member.enrollUnivInfo(createEnrollUnivRequest());
        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        member.enrollUnivInfo(createEnrollUnivRequest("홍익대학교", "test@hongik.ac.kr"));

        // when /then
        assertThat(member.isAuthorized()).isFalse();
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("최종 인증에 성공 후, 학교 이메일 외 다른 정보를 수정하면 인증은 해제되지 않는다.")
    void updateUnivInfo_Then_Keep_Auth() {
        // given
        var request = createEnrollUnivRequest();
        member.enrollUnivInfo(request);
        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        member.enrollUnivInfo(new EnrollUnivRequest(
                request.univName(),
                request.univMail(),
                false,
                2020,
                "인공지능응용학과"
        ));

        // when /then
        assertThat(member.isAuthorized()).isTrue();
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("학교 인증만 했을 경우 최종 인증에 실패한다")
    void isAuthorized_Fail_When_UnivOnly() {
        // given
        member.enrollUnivInfo(createEnrollUnivRequest());
        member.completeUnivAuthentication();

        // when & then
        assertThat(member.isAuthorized()).isFalse();
    }

    @Test
    @DisplayName("라이엇 계정 인증만 했을 경우 최종 인증에 실패한다")
    void isAuthorized_Fail_When_RiotOnly() {
        // given
        member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        // when & then
        assertThat(member.isAuthorized()).isFalse();
    }

    @Test
    @DisplayName("학교 정보를 정상적으로 인증(업데이트)한다")
    void enrollUnivInfo() {
        // given
        EnrollUnivRequest request = createEnrollUnivRequest();

        // when
        member.enrollUnivInfo(request);

        // then
        assertThat(member.getUnivInfo()).isNotNull();
        assertThat(member.getUnivInfo().univName()).isEqualTo(request.univName());
    }

    @Test
    @DisplayName("학교 인증 정보를 제거한다")
    void removeUnivInfo() {
        // given
        member.enrollUnivInfo(createEnrollUnivRequest());
        assertThat(member.getUnivInfo()).isNotNull();

        // when
        member.removeUnivInfo();

        // then
        assertThat(member.getUnivInfo()).isNull();
        assertThat(member.getAuthStatus()).isEqualTo(MemberAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("라이엇 소환사 정보를 정상적으로 인증(업데이트)한다")
    void connectSummonerInfo() {
        // given
        var request = createRiotAuthRequest();

        // when
        member.connectSummonerInfo(createSummonerInfoConnector(), request);

        // then
        assertThat(member.getSummonerInfo()).isNotNull();
        assertThat(member.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
    }

    @Test
    @DisplayName("라이엇 소환사 인증 정보를 제거한다")
    void removeRiotAuthentication() {
        // given
        member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());
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
        member.enrollUnivInfo(createEnrollUnivRequest());
        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());
        assertThat(member.isAuthorized()).isTrue();

        // when & then
        assertThatThrownBy(() -> member.completeUnivAuthentication())
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateProfile() {
        // given
        var request = new MemberProfileUpdateRequest(
                "updated",
                "description",
                LolPosition.TOP,
                LolPosition.JG
        );

        // when
        member.updateProfile(request);

        // then
        assertThat(member.getUsername()).isEqualTo("updated");
        assertThat(member.getDescription()).isEqualTo("description");
        assertThat(member.getMainPosition()).isEqualTo(LolPosition.TOP);
        assertThat(member.getSubPosition()).isEqualTo(LolPosition.JG);
    }
}