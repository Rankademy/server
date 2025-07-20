package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.webapi.dto.ProfileResponse;
import maruhxn.rankademy.application.user.provided.UserAuthorizer;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me") // TODO: 변경 필요
public class ProfileApi {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final UserAuthorizer userAuthorizer;

    @GetMapping
    public ProfileResponse getProfile(
            @AuthenticationPrincipal RankademyUser rankademyUser
            ) {
        User user = userReader.find(rankademyUser.getId());
        return ProfileResponse.from(user);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid ProfileUpdateRequest profileUpdateRequest
    ) {
        userWriter.updateProfile(rankademyUser.getId(), profileUpdateRequest);
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

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal RankademyUser rankademyUser) {
        userWriter.withdraw(rankademyUser.getId());
    }
}
