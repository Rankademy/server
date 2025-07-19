package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.member.provided.MemberWriter;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.MemberFixture;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
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
    MemberRepository memberRepository;
    @Autowired
    MemberWriter memberWriter;

    @Autowired
    EntityManager em;

    @Test
    void register() throws JsonProcessingException, UnsupportedEncodingException {
        MemberRegisterRequest request = MemberFixture.createMemberRegisterRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        assertThat(result)
                .hasStatus(HttpStatus.CREATED)
                .body()
                .isNotNull();

        Member member = memberRepository.findById(Long.parseLong(result.getResponse().getContentAsString())).orElseThrow();
        assertAll(
                () -> assertThat(member.getEmail().address()).isEqualTo(request.email()),
                () -> assertThat(member.getUsername()).isEqualTo(request.username()),
                () -> assertThat(member.getPasswordHash()).isNotNull()
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