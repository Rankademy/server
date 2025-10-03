package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.application.user.provided.UserAuthorizer;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class ProfileApi {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final UserAuthorizer userAuthorizer;

    @GetMapping
    public ProfileResponse getProfile(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        return userReader.getProfile(rankademyUser.getId());
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid ProfileUpdateRequest profileUpdateRequest
    ) {
        userWriter.updateProfile(rankademyUser.getId(), profileUpdateRequest);
    }

    @PostMapping("/univ")
    public void enrollUnivInfo(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid EnrollUnivRequest enrollUnivRequest
    ) {
        userWriter.enrollUnivInfo(rankademyUser.getId(), enrollUnivRequest);
    }

    @DeleteMapping("/univ")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUnivInfo(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        userWriter.removeUnivInfo(rankademyUser.getId());
    }

    @PostMapping("/univ-email/send")
    public void sendCertifyUnivMail(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        userAuthorizer.sendUnivCertifyMail(rankademyUser.getId());
    }

    @PostMapping("/univ-email/certify")
    public void certifyUnivMail(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestParam(name = "code", required = true) int code
    ) {
        userAuthorizer.completeUnivAuthentication(rankademyUser.getId(), code);
    }

    @PostMapping("/rso")
    public void rso(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid RiotAuthRequest riotAuthRequest
    ) {
        userAuthorizer.completeRiotAuthentication(rankademyUser.getId(), riotAuthRequest);
    }

    @DeleteMapping("/rso")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRso(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        userAuthorizer.removeRiotAuthentication(rankademyUser.getId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal RankademyUser rankademyUser) {
        userWriter.withdraw(rankademyUser.getId());
    }
}
