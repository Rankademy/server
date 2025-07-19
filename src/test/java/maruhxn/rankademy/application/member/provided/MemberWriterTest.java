package maruhxn.rankademy.application.member.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.member.required.EmailSender;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.domain.member.LolPosition;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.PasswordEncoder;
import maruhxn.rankademy.domain.member.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberProfileUpdateRequest;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
import maruhxn.rankademy.domain.member.exception.DuplicateUsernameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.member.MemberFixture.createEnrollUnivRequest;
import static maruhxn.rankademy.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class MemberWriterTest {

    @Autowired
    MemberWriter memberWriter;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    EmailSender emailSender;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("신규 회원이 정상적으로 가입된다.")
    void register_NewMember() {
        // given
        var request = createMemberRegisterRequest();

        // when
        Member member = memberWriter.register(request);

        // then
        assertThat(member).isNotNull();
        assertThat(member.getUsername()).isEqualTo("maruhxn");
        assertThat(member.getEmail().address()).isEqualTo("maruhxn@rankademy.app");

        // 환영 이메일이 발송되었는지 검증
        verify(emailSender, times(1)).send(
                eq(member.getEmail()),
                any(String.class),
                any(String.class)
        );
    }

    @Test
    @DisplayName("중복된 아이디(username)로 가입 시 예외가 발생한다.")
    void register_FailWithDuplicateUsername() {
        // given
        Member member = registerMember();

        // 다른 이메일, 하지만 동일한 아이디로 가입 시도
        var duplicateRequest = new MemberRegisterRequest(
                "another.user@rankademy.app",
                member.getUsername(),
                "password456"
        );

        // when & then
        assertThatThrownBy(() -> memberWriter.register(duplicateRequest))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    @DisplayName("기존에 가입된 이메일로 다시 가입 시 비밀번호가 변경된다.")
    void register_ExistingMember_ChangesPassword() {
        // given
        Member member = registerMember();

        // 동일한 이메일, 새로운 비밀번호로 다시 가입 요청
        var updateRequest = new MemberRegisterRequest(
                member.getEmail().address(),
                member.getUsername(),
                "newsecret"
        );

        // when
        Member updatedMember = memberWriter.register(updateRequest);
        em.flush();

        // then
        assertThat(updatedMember.verifyPassword("newsecret", passwordEncoder)).isTrue(); // 간단한 PasswordEncoder 모킹

        // 환영 이메일은 발송되지 않아야 함
        verify(emailSender, times(1)).send(any(), any(), any()); // 초기 가입 때 1번만 호출
    }

    @Test
    void enrollUnivInfo() {
        Member member = registerMember();

        EnrollUnivRequest enrollUnivRequest = createEnrollUnivRequest();

        member = memberWriter.enrollUnivInfo(member.getId(), enrollUnivRequest);
        em.flush();

        assertThat(member.getUnivInfo().univName()).isEqualTo(enrollUnivRequest.univName());
        assertThat(member.getUnivInfo().univVerified()).isEqualTo(false);
    }

    private Member registerMember() {
        var initialRequest = createMemberRegisterRequest();
        Member member = memberWriter.register(initialRequest);
        em.flush();
        em.clear();
        return member;
    }

    @Test
    void removeUnivInfo() {
        Member member = registerMember();

        member = memberWriter.removeUnivInfo(member.getId());
        em.flush();

        assertThat(member.getUnivInfo()).isNull();
        assertThat(member.isAuthorized()).isFalse();
    }

    @Test
    void updateProfile() {
        Member member = registerMember();

        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(
                "newname",
                "자기소개입니다.",
                LolPosition.TOP,
                LolPosition.JG
        );

        member = memberWriter.updateProfile(member.getId(), request);
        em.flush();

        assertThat(member.getUsername()).isEqualTo(request.username());
        assertThat(member.getDescription()).isEqualTo(request.description());
        assertThat(member.getMainPosition()).isEqualTo(request.mainPosition());
        assertThat(member.getSubPosition()).isEqualTo(request.subPosition());
    }

    @Test
    void withdraw() {
        Member member = registerMember();

        memberWriter.withdraw(member.getId());
        em.flush();

        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }
}
