package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.security.dto.TokenDto;
import maruhxn.rankademy.adapter.security.jwt.JwtProvider;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import static maruhxn.rankademy.adapter.security.Constants.REFRESH_TOKEN_HEADER;
import static maruhxn.rankademy.domain.user.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@AutoConfigureMockMvc
class AuthApiTest extends IntegrationTestSupport {

    static final String BASE_URL = "/api/v1/auth";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    EntityManager em;

    @Test
    void refresh() throws UnsupportedEncodingException, JsonProcessingException {
        User user = createUser();
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail().address(), new Date());
        user.addRefreshToken(refreshToken);
        userRepository.save(user);
        em.flush();
        em.clear();

        assertThat(user.getRefreshTokens()).hasSize(1);

        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .header(REFRESH_TOKEN_HEADER, "Bearer " + refreshToken)
                .exchange();

        assertThat(result)
                .hasStatusOk();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TokenDto.class);
        User target = userRepository.findByEmail(new Email(response.email())).orElseThrow();
        assertAll(
                () -> assertThat(response.email()).isEqualTo(target.getEmail().address()),
                () -> assertThat(response.accessToken()).isNotNull(),
                () -> assertThat(response.refreshToken()).isNotNull(),
                () -> assertThat(target.getRefreshTokens()).hasSize(1)
        );
    }
}
