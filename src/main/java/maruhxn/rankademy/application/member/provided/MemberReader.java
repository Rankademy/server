package maruhxn.rankademy.application.member.provided;

import maruhxn.rankademy.domain.member.Member;

public interface MemberReader {

    Member find(Long memberId);
}
