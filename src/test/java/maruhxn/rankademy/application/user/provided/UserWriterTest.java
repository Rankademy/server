package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.OAuth2Provider;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.user.UserFixture.createEnrollUnivRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UserWriterTest extends IntegrationTestSupport {

    @Autowired
    UserWriter userWriter;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    EmailSender emailSender;

    @Test
    @DisplayName("소셜 회원가입 시 환영 메일이 발송된다.")
    void oauth2Register_NewUser() {
        // given
        var request = new UserOAuth2CreateRequest(
                "social.user@rankademy.app",
                "social_user",
                OAuth2Provider.GOOGLE,
                "oauth-id-123"
        );

        // when
        User user = userWriter.oauth2Register(request);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getOauthAccounts()).isNotEmpty();
        verify(emailSender, times(1)).send(
                eq(user.getEmail()),
                any(String.class),
                any(String.class)
        );
    }

    @Test
    void enrollUnivInfo() {
        User user = oauth2RegisterUser();

        EnrollUnivRequest enrollUnivRequest = createEnrollUnivRequest();

        user = userWriter.enrollUnivInfo(user.getId(), enrollUnivRequest);
        em.flush();

        assertThat(user.getUnivInfo().getUnivName()).isEqualTo(enrollUnivRequest.univName());
        assertThat(user.getUnivInfo().isUnivVerified()).isEqualTo(false);
    }

    @Test
    void enrollUnivInfo_Validation_Fail() {
        User user = oauth2RegisterUser();

        EnrollUnivRequest enrollUnivRequest = createEnrollUnivRequest("서울과학기술대학교", "test@test.ac.kr");

        assertThatThrownBy(() -> userWriter.enrollUnivInfo(user.getId(), enrollUnivRequest))
                .isInstanceOf(IllegalArgumentException.class);


        EnrollUnivRequest enrollUnivRequest2 = createEnrollUnivRequest("없는대학교", "test@test.ac.kr");

        assertThatThrownBy(() -> userWriter.enrollUnivInfo(user.getId(), enrollUnivRequest2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User oauth2RegisterUser() {
        var request = new UserOAuth2CreateRequest(
                "user" + System.nanoTime() + "@rankademy.app",
                "user" + System.nanoTime(),
                OAuth2Provider.GOOGLE,
                "oauth-id-" + System.nanoTime()
        );
        User user = userWriter.oauth2Register(request);
        em.flush();
        em.clear();
        return user;
    }

    @Test
    void removeUnivInfo() {
        User user = oauth2RegisterUser();

        user = userWriter.removeUnivInfo(user.getId());
        em.flush();

        assertThat(user.getUnivInfo()).isNull();
        assertThat(user.isAuthorized()).isFalse();
    }

    @Test
    void updateProfile() {
        User user = oauth2RegisterUser();

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
        User user = oauth2RegisterUser();

        userWriter.withdraw(user.getId());
        em.flush();

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}
