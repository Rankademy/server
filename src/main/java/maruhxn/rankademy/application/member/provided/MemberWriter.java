package maruhxn.rankademy.application.member.provided;

import jakarta.validation.Valid;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberProfileUpdateRequest;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;

/**
 * 회원 등록과 관련된 기능 제공
 */
public interface MemberWriter {

    Member register(@Valid MemberRegisterRequest registerRequest);

    Member enrollUnivInfo(Long memberId, @Valid EnrollUnivRequest enrollUnivRequest);

    Member removeUnivInfo(Long memberId);

    Member updateProfile(Long memberId, @Valid MemberProfileUpdateRequest updateProfileRequest);

    void withdraw(Long memberId);
}
