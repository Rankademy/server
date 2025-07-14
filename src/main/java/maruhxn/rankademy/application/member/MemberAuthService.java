package maruhxn.rankademy.application.member;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.member.provided.MemberAuthorizer;
import maruhxn.rankademy.application.member.provided.MemberReader;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.application.member.required.UnivMailCertifier;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.service.SummonerInfoConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAuthService implements MemberAuthorizer {

    private final MemberReader memberReader;
    private final MemberRepository memberRepository;
    private final UnivMailCertifier univMailCertifier;
    private final SummonerInfoConnector summonerInfoConnector;

    @Override
    public void sendUnivCertifyMail(Long memberId) {
        Member member = memberReader.find(memberId);

        if (member.getUnivInfo() == null) {
            throw new IllegalStateException("학교 정보를 등록해주세요.");
        }

        univMailCertifier.sendCertifyMail(
                member.getUnivInfo().univMail().address(),
                member.getUnivInfo().univName(),
                member.getUnivInfo().inCollege()
        );
    }

    @Override
    @Transactional
    public Member completeUnivAuthentication(Long memberId, int code) {
        Member member = memberReader.find(memberId);

        univMailCertifier.certifyCode(
                member.getUnivInfo().univMail().address(),
                member.getUnivInfo().univName(),
                code
        );

        member.completeUnivAuthentication();

        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public Member completeRiotAuthentication(Long memberId, RiotAuthRequest riotAuthRequest) {
        Member member = memberReader.find(memberId);
        member.connectSummonerInfo(summonerInfoConnector, riotAuthRequest);
        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public Member removeRiotAuthentication(Long memberId) {
        Member member = memberReader.find(memberId);
        member.removeRiotAuthentication();
        return memberRepository.save(member);
    }
}
