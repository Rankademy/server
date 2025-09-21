package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static maruhxn.rankademy.domain.user.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
@DisplayName("Notification API 테스트")
class NotificationApiTest {

    static final String BASE_URL = "/api/v1/notifications";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser("test@test.com", "tester");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공")
    void getNotifications_success() throws Exception {
        // given
        Notification notification = Notification.create(testUser.getId(), "noti", LocalDateTime.now());
        notificationRepository.save(notification);

        // when
        var result = mvcTester.get().uri(BASE_URL + "?page=0")
                .with(user(RankademyUser.from(UserInfo.from(testUser))))
                .exchange();

        // then
        NotificationPageResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), NotificationPageResponse.class);
        assertThat(result).hasStatusOk();
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.notifications()).hasSize(1);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("알림 목록 조회 - 인증되지 않은 사용자")
    void getNotifications_withAnonymousUser() {
        // when
        var result = mvcTester.get().uri(BASE_URL + "?page=0")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void confirmNotification_success() {
        // given
        Notification notification = Notification.create(testUser.getId(), "noti", LocalDateTime.now());
        notificationRepository.save(notification);

        // when
        var result = mvcTester.patch().uri(BASE_URL + "/" + notification.getId() + "/confirm")
                .with(user(RankademyUser.from(UserInfo.from(testUser))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 권한 없음")
    void confirmNotification_forbidden() {
        // given
        Notification notification = Notification.create(999L, "noti", LocalDateTime.now());
        notificationRepository.save(notification);

        // when
        var result = mvcTester.patch().uri(BASE_URL + "/" + notification.getId() + "/confirm")
                .with(user(RankademyUser.from(UserInfo.from(testUser))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("알림 읽음 처리 - 인증되지 않은 사용자")
    void confirmNotification_withAnonymousUser() {
        // given
        Long notificationId = 1L;

        // when
        var result = mvcTester.patch().uri(BASE_URL + "/" + notificationId + "/confirm")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
