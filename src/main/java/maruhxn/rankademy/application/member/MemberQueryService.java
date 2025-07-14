package maruhxn.rankademy.application.member;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.member.provided.MemberReader;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.domain.member.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService implements MemberReader {

    private final MemberRepository memberRepository;

    @Override
    public Member find(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id: " + memberId));
    }
}
