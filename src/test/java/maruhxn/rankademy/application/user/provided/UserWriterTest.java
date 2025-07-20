package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.PasswordEncoder;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
import maruhxn.rankademy.domain.user.exception.DuplicateUsernameException;
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
    void register_NewUser() {
        // given
        var request = createUserRegisterRequest();

        // when
        User user = userWriter.register(request);

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
    void register_FailWithDuplicateUsername() {
        // given
        User user = registerUser();

        // 다른 이메일, 하지만 동일한 아이디로 가입 시도
        var duplicateRequest = new UserRegisterRequest(
                "another.user@rankademy.app",
                user.getUsername(),
                "password456"
        );

        // when & then
        assertThatThrownBy(() -> userWriter.register(duplicateRequest))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    @DisplayName("기존에 가입된 이메일로 다시 가입 시 비밀번호가 변경된다.")
    void register_ExistingUser_ChangesPassword() {
        // given
        User user = registerUser();

        // 동일한 이메일, 새로운 비밀번호로 다시 가입 요청
        var updateRequest = new UserRegisterRequest(
                user.getEmail().address(),
                user.getUsername(),
                "newsecret"
        );

        // when
        User updatedUser = userWriter.register(updateRequest);
        em.flush();

        // then
        assertThat(updatedUser.verifyPassword("newsecret", passwordEncoder)).isTrue(); // 간단한 PasswordEncoder 모킹

        // 환영 이메일은 발송되지 않아야 함
        verify(emailSender, times(1)).send(any(), any(), any()); // 초기 가입 때 1번만 호출
    }

    @Test
    void enrollUnivInfo() {
        User user = registerUser();

        EnrollUnivRequest enrollUnivRequest = createEnrollUnivRequest();

        user = userWriter.enrollUnivInfo(user.getId(), enrollUnivRequest);
        em.flush();

        assertThat(user.getUnivInfo().univName()).isEqualTo(enrollUnivRequest.univName());
        assertThat(user.getUnivInfo().univVerified()).isEqualTo(false);
    }

    private User registerUser() {
        var initialRequest = createUserRegisterRequest();
        User user = userWriter.register(initialRequest);
        em.flush();
        em.clear();
        return user;
    }

    @Test
    void removeUnivInfo() {
        User user = registerUser();

        user = userWriter.removeUnivInfo(user.getId());
        em.flush();

        assertThat(user.getUnivInfo()).isNull();
        assertThat(user.isAuthorized()).isFalse();
    }

    @Test
    void updateProfile() {
        User user = registerUser();

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
        User user = registerUser();

        userWriter.withdraw(user.getId());
        em.flush();

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}
