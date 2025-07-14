package maruhxn.rankademy.application.member.required;

import maruhxn.rankademy.domain.member.Email;
import maruhxn.rankademy.domain.member.Member;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * 회원 정보를 저장하거나 조회한다
 */
public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    Optional<Member> findById(Long memberId);

    Optional<Member> findByEmail(Email email);

    Optional<Member> findByUsername(String username);

    void delete(Member member);

}
