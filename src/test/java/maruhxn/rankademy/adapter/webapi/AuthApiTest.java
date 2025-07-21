package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.dto.TokenDto;
import maruhxn.rankademy.adapter.security.jwt.JwtProvider;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import static maruhxn.rankademy.adapter.security.Constants.REFRESH_TOKEN_HEADER;
import static maruhxn.rankademy.domain.user.UserFixture.createUser;
import static maruhxn.rankademy.domain.user.UserFixture.createUserRegisterRequest;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class AuthApiTest {

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
    void register() throws JsonProcessingException, UnsupportedEncodingException {
        UserRegisterRequest request = createUserRegisterRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        assertThat(result)
                .hasStatus(HttpStatus.CREATED)
                .body()
                .isNotNull();

        User user = userRepository.findById(Long.parseLong(result.getResponse().getContentAsString())).orElseThrow();
        assertAll(
                () -> assertThat(user.getEmail().address()).isEqualTo(request.email()),
                () -> assertThat(user.getUsername()).isEqualTo(request.username()),
                () -> assertThat(user.getPasswordHash()).isNotNull()
        );
    }

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

//    @Test
//    void register_FAIL() throws JsonProcessingException, UnsupportedEncodingException {
//        Member member = MemberFixture.createMember();
//        memberRepository.save(member);
//        em.flush();
//        em.clear();
//
//        MemberRegisterRequest request = MemberFixture.createMemberRegisterRequest();
//        String requestJson = objectMapper.writeValueAsString(request);
//
//        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestJson)
//                .exchange();
//
//        assertThat(result)
//                .hasStatus(HttpStatus.BAD_REQUEST)
//                .bodyJson();
//
////        var problemDetail = objectMapper.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);
//    }
}