package maruhxn.rankademy.application.member.provided;

import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;

public interface MemberAuthorizer {

    void sendUnivCertifyMail(Long memberId);

    Member completeUnivAuthentication(Long memberId, int code);

    Member completeRiotAuthentication(Long memberId, RiotAuthRequest riotAuthRequest);

    Member removeRiotAuthentication(Long memberId);

}
