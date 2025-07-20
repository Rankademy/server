package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
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
    UserWriter userWriter;

    @Autowired
    EntityManager em;

    @Test
    void register() throws JsonProcessingException, UnsupportedEncodingException {
        UserRegisterRequest request = UserFixture.createUserRegisterRequest();
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