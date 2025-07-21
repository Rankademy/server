package maruhxn.rankademy.domain.user;

import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    User user;
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        this.passwordEncoder = createPasswordEncoder();
        user = User.register(
                createUserRegisterRequest(),
                passwordEncoder
        );
    }

    @Test
    @DisplayName("정상적으로 회원가입을 한다")
    void registerUser() {
        assertThat(user.getAuthStatus()).isEqualTo(UserAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("유효하지 않은 이메일 형식으로 회원가입 시 예외가 발생한다")
    void registerFailByInvalidEmail() {
        assertThatThrownBy(() ->
                User.register(createUserRegisterRequest("invalid email"), passwordEncoder)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호를 올바르게 검증한다")
    void verifyPassword() {
        assertThat(user.verifyPassword("verysecret", passwordEncoder)).isTrue();
        assertThat(user.verifyPassword("hello", passwordEncoder)).isFalse();
    }

    @Test
    @DisplayName("비밀번호를 변경한다")
    void changePassword() {
        user.changePassword("verysecret2", passwordEncoder);
        assertThat(user.verifyPassword("verysecret2", passwordEncoder)).isTrue();
    }

    @Test
    @DisplayName("학교 및 라이엇 계정 인증 완료 시 최종 인증에 성공한다")
    void isAuthorized_Success() {
        // given
        user.enrollUnivInfo(createEnrollUnivRequest());
        user.completeUnivAuthentication();
        user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        // when /then
        assertThat(user.isAuthorized()).isTrue();
        assertThat(user.getAuthStatus()).isEqualTo(UserAuthStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("최종 인증에 성공 후, 학교 이메일을 수정하면 인증이 해제된다.")
    void updateUnivInfo_Then_Auth_Fail() {
        // given
        user.enrollUnivInfo(createEnrollUnivRequest());
        user.completeUnivAuthentication();
        user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        user.enrollUnivInfo(createEnrollUnivRequest("홍익대학교", "test@hongik.ac.kr"));

        // when /then
        assertThat(user.isAuthorized()).isFalse();
        assertThat(user.getAuthStatus()).isEqualTo(UserAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("최종 인증에 성공 후, 학교 이메일 외 다른 정보를 수정하면 인증은 해제되지 않는다.")
    void updateUnivInfo_Then_Keep_Auth() {
        // given
        var request = createEnrollUnivRequest();
        user.enrollUnivInfo(request);
        user.completeUnivAuthentication();
        user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        user.enrollUnivInfo(new EnrollUnivRequest(
                request.univName(),
                request.univMail(),
                false,
                2020,
                "인공지능응용학과"
        ));

        // when /then
        assertThat(user.isAuthorized()).isTrue();
        assertThat(user.getAuthStatus()).isEqualTo(UserAuthStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("학교 인증만 했을 경우 최종 인증에 실패한다")
    void isAuthorized_Fail_When_UnivOnly() {
        // given
        user.enrollUnivInfo(createEnrollUnivRequest());
        user.completeUnivAuthentication();

        // when & then
        assertThat(user.isAuthorized()).isFalse();
    }

    @Test
    @DisplayName("라이엇 계정 인증만 했을 경우 최종 인증에 실패한다")
    void isAuthorized_Fail_When_RiotOnly() {
        // given
        user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());

        // when & then
        assertThat(user.isAuthorized()).isFalse();
    }

    @Test
    @DisplayName("학교 정보를 정상적으로 인증(업데이트)한다")
    void enrollUnivInfo() {
        // given
        EnrollUnivRequest request = createEnrollUnivRequest();

        // when
        user.enrollUnivInfo(request);

        // then
        assertThat(user.getUnivInfo()).isNotNull();
        assertThat(user.getUnivInfo().univName()).isEqualTo(request.univName());
    }

    @Test
    @DisplayName("학교 인증 정보를 제거한다")
    void removeUnivInfo() {
        // given
        user.enrollUnivInfo(createEnrollUnivRequest());
        assertThat(user.getUnivInfo()).isNotNull();

        // when
        user.removeUnivInfo();

        // then
        assertThat(user.getUnivInfo()).isNull();
        assertThat(user.getAuthStatus()).isEqualTo(UserAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("라이엇 소환사 정보를 정상적으로 인증(업데이트)한다")
    void connectSummonerInfo() {
        // given
        var request = createRiotAuthRequest();

        // when
        user.connectSummonerInfo(createSummonerInfoConnector(), request);

        // then
        assertThat(user.getSummonerInfo()).isNotNull();
        assertThat(user.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
    }

    @Test
    @DisplayName("라이엇 소환사 인증 정보를 제거한다")
    void removeRiotAuthentication() {
        // given
        user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());
        assertThat(user.getSummonerInfo()).isNotNull();

        // when
        user.removeRiotAuthentication();

        // then
        assertThat(user.getSummonerInfo()).isNull();
        assertThat(user.getAuthStatus()).isEqualTo(UserAuthStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("이미 최종 인증된 멤버가 다시 인증을 시도하면 예외가 발생한다")
    void reAuthentication_Fail() {
        // given
        user.enrollUnivInfo(createEnrollUnivRequest());
        user.completeUnivAuthentication();
        user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());
        assertThat(user.isAuthorized()).isTrue();

        // when & then
        assertThatThrownBy(() -> user.completeUnivAuthentication())
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateProfile() {
        // given
        var request = new ProfileUpdateRequest(
                "updated",
                "description",
                LolPosition.TOP,
                LolPosition.JG
        );

        // when
        user.updateProfile(request);

        // then
        assertThat(user.getUsername()).isEqualTo("updated");
        assertThat(user.getDescription()).isEqualTo("description");
        assertThat(user.getMainPosition()).isEqualTo(LolPosition.TOP);
        assertThat(user.getSubPosition()).isEqualTo(LolPosition.JG);
    }

    @Test
    void updateTitles() {
        user = createUser(1L);

        user.updateTitles(createTitleProvider());

        assertThat(user.getTitles()).hasSize(1);
    }
}