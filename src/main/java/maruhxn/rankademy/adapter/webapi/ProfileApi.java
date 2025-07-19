package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.ProfileResponse;
import maruhxn.rankademy.application.member.provided.MemberAuthorizer;
import maruhxn.rankademy.application.member.provided.MemberReader;
import maruhxn.rankademy.application.member.provided.MemberWriter;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.dto.MemberProfileUpdateRequest;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/{memberId}") // TODO: 변경 필요
public class ProfileApi {

    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final MemberAuthorizer memberAuthorizer;

    @GetMapping
    public ProfileResponse getProfile(
            @PathVariable("memberId") Long memberId
    ) {
        Member member = memberReader.find(memberId);
        return ProfileResponse.from(member);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @PathVariable("memberId") Long memberId,
            @RequestBody @Valid MemberProfileUpdateRequest profileUpdateRequest
    ) {
        memberWriter.updateProfile(memberId, profileUpdateRequest);
    }

    @PostMapping("/univ-email/send")
    public void sendCertifyUnivMail(
            @PathVariable("memberId") Long memberId
    ) {
        memberAuthorizer.sendUnivCertifyMail(memberId);
    }

    @PostMapping("/univ-email/certify")
    public void certifyUnivMail(
            @PathVariable("memberId") Long memberId,
            @RequestParam(name = "code", required = true) int code
    ) {
        memberAuthorizer.completeUnivAuthentication(memberId, code);
    }

    @PostMapping("/rso")
    public void rso(
            @PathVariable("memberId") Long memberId,
            @RequestBody @Valid RiotAuthRequest riotAuthRequest
    ) {
        memberAuthorizer.completeRiotAuthentication(memberId, riotAuthRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable("memberId") Long memberId) {
        memberWriter.withdraw(memberId);
    }
}
