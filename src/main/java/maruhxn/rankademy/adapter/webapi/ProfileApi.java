package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.ProfileResponse;
import maruhxn.rankademy.application.user.provided.UserAuthorizer;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/{userId}") // TODO: 변경 필요
public class ProfileApi {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final UserAuthorizer userAuthorizer;

    @GetMapping
    public ProfileResponse getProfile(
            @PathVariable("userId") Long userId
    ) {
        User user = userReader.find(userId);
        return ProfileResponse.from(user);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid ProfileUpdateRequest profileUpdateRequest
    ) {
        userWriter.updateProfile(userId, profileUpdateRequest);
    }

    @PostMapping("/univ-email/send")
    public void sendCertifyUnivMail(
            @PathVariable("userId") Long userId
    ) {
        userAuthorizer.sendUnivCertifyMail(userId);
    }

    @PostMapping("/univ-email/certify")
    public void certifyUnivMail(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "code", required = true) int code
    ) {
        userAuthorizer.completeUnivAuthentication(userId, code);
    }

    @PostMapping("/rso")
    public void rso(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid RiotAuthRequest riotAuthRequest
    ) {
        userAuthorizer.completeRiotAuthentication(userId, riotAuthRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable("userId") Long userId) {
        userWriter.withdraw(userId);
    }
}
