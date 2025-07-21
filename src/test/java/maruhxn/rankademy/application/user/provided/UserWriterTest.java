package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.OAuth2Provider;
import maruhxn.rankademy.domain.user.PasswordEncoder;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.user.UserFixture.createEnrollUnivRequest;
import static maruhxn.rankademy.domain.user.UserFixture.createUserRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class UserWriterTest {

    @Autowired
    UserWriter userWriter;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    EmailSender emailSender;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("신규 회원이 정상적으로 가입된다.")
    void register_OrSetPassword_NewUser() {
        // given
        var request = createUserRegisterRequest();

        // when
        User user = userWriter.registerOrSetPassword(request);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("maruhxn");
        assertThat(user.getEmail().address()).isEqualTo("maruhxn@rankademy.app");

        // 환영 이메일이 발송되었는지 검증
        verify(emailSender, times(1)).send(
                eq(user.getEmail()),
                any(String.class),
                any(String.class)
        );
    }

    @Test
    @DisplayName("중복된 아이디(username)로 가입 시 예외가 발생한다.")
    void register_OrSetPassword_FailWithDuplicateUsername() {
        // given
        User user = registerOrSetPasswordUser();

        // 다른 이메일, 하지만 동일한 아이디로 가입 시도
        var duplicateRequest = new UserRegisterRequest(
                "another.user@rankademy.app",
                user.getUsername(),
                "password456"
        );

        // when & then
        assertThatThrownBy(() -> userWriter.registerOrSetPassword(duplicateRequest))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("소셜가입 이후 동일 이메일 회원 가입 시 비밀번호 설정된다.")
    void register_OrSetPassword_ExistingUser_ChangesPassword() {
        // given
        User user = User.oauth2Register(new UserOAuth2CreateRequest("test@test.com", "소셜유저", OAuth2Provider.GOOGLE, "12345"));
        userRepository.save(user);
        em.flush();
        em.clear();

        // 동일한 이메일, 새로운 비밀번호로 다시 가입 요청
        var updateRequest = new UserRegisterRequest(
                "test@test.com",
                "소셜유저",
                "verysecret"
        );

        // when
        User updatedUser = userWriter.registerOrSetPassword(updateRequest);
        em.flush();

        // then
        assertThat(updatedUser.getOauthAccounts()).isNotEmpty();
        assertThat(updatedUser.verifyPassword("verysecret", passwordEncoder)).isTrue();
    }

    @Test
    void enrollUnivInfo() {
        User user = registerOrSetPasswordUser();

        EnrollUnivRequest enrollUnivRequest = createEnrollUnivRequest();

        user = userWriter.enrollUnivInfo(user.getId(), enrollUnivRequest);
        em.flush();

        assertThat(user.getUnivInfo().univName()).isEqualTo(enrollUnivRequest.univName());
        assertThat(user.getUnivInfo().univVerified()).isEqualTo(false);
    }

    @Test
    void enrollUnivInfo_Validation_Fail() {
        User user = registerOrSetPasswordUser();

        EnrollUnivRequest enrollUnivRequest = createEnrollUnivRequest("서울과학기술대학교", "test@test.ac.kr");

        assertThatThrownBy(() -> userWriter.enrollUnivInfo(user.getId(), enrollUnivRequest))
            .isInstanceOf(IllegalArgumentException.class);


        EnrollUnivRequest enrollUnivRequest2 = createEnrollUnivRequest("없는대학교", "test@test.ac.kr");

        assertThatThrownBy(() -> userWriter.enrollUnivInfo(user.getId(), enrollUnivRequest2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User registerOrSetPasswordUser() {
        var initialRequest = createUserRegisterRequest();
        User user = userWriter.registerOrSetPassword(initialRequest);
        em.flush();
        em.clear();
        return user;
    }

    @Test
    void removeUnivInfo() {
        User user = registerOrSetPasswordUser();

        user = userWriter.removeUnivInfo(user.getId());
        em.flush();

        assertThat(user.getUnivInfo()).isNull();
        assertThat(user.isAuthorized()).isFalse();
    }

    @Test
    void updateProfile() {
        User user = registerOrSetPasswordUser();

        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "newname",
                "자기소개입니다.",
                LolPosition.TOP,
                LolPosition.JG
        );

        user = userWriter.updateProfile(user.getId(), request);
        em.flush();

        assertThat(user.getUsername()).isEqualTo(request.username());
        assertThat(user.getDescription()).isEqualTo(request.description());
        assertThat(user.getMainPosition()).isEqualTo(request.mainPosition());
        assertThat(user.getSubPosition()).isEqualTo(request.subPosition());
    }

    @Test
    void withdraw() {
        User user = registerOrSetPasswordUser();

        userWriter.withdraw(user.getId());
        em.flush();

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}
