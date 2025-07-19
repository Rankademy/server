package maruhxn.rankademy.application.member;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.member.provided.MemberReader;
import maruhxn.rankademy.application.member.provided.MemberWriter;
import maruhxn.rankademy.application.member.required.EmailSender;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.domain.member.Email;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.PasswordEncoder;
import maruhxn.rankademy.domain.member.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberProfileUpdateRequest;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
import maruhxn.rankademy.domain.member.exception.DuplicateUsernameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class MemberModifyService implements MemberWriter {

    private final MemberReader memberReader;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    @Override
    public Member register(MemberRegisterRequest registerRequest) {
        Optional<Member> optionalMember = memberRepository.findByEmail(new Email(registerRequest.email()));

        Member member;
        if (optionalMember.isPresent()) {
            // TODO: 같은 이메일로 이메일 회원가입을 중복 진행하는 경우 막기.
            member = optionalMember.get();
            member.changePassword(registerRequest.password(), passwordEncoder);
        } else {
            this.checkDuplicateUsername(registerRequest.username());

            member = Member.register(registerRequest, passwordEncoder);

            this.sendWelcomeEmail(member);
        }

        return memberRepository.save(member);
    }

    private void sendWelcomeEmail(Member member) {
        emailSender.send(
                member.getEmail(),
                "등록을 완료해주세요.",
                String.format("안녕하세요, %s님!%nRankademy에 가입해 주셔서 진심으로 감사합니다.", member.getUsername())
        );
    }

    private void checkDuplicateUsername(String username) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException(username);
        }
    }

    @Override
    public Member enrollUnivInfo(Long memberId, EnrollUnivRequest enrollUnivRequest) {
        Member member = memberReader.find(memberId);
        // TODO: 이메일 일치 여부 확인 로직 추가 필요
        member.enrollUnivInfo(enrollUnivRequest);
        return memberRepository.save(member);
    }

    @Override
    public Member removeUnivInfo(Long memberId) {
        Member member = memberReader.find(memberId);
        member.removeUnivInfo();
        return memberRepository.save(member);
    }

    @Override
    public Member updateProfile(Long memberId, MemberProfileUpdateRequest updateProfileRequest) {
        Member member = memberReader.find(memberId);

        this.checkDuplicateUsername(updateProfileRequest.username());

        member.updateProfile(updateProfileRequest);

        return memberRepository.save(member);
    }

    @Override
    public void withdraw(Long memberId) {
        Member member = memberReader.find(memberId);
        memberRepository.delete(member);
    }

}

