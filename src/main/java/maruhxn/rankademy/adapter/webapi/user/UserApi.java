package maruhxn.rankademy.adapter.webapi.user;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.application.user.provided.UserReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApi {

    private final UserReader userReader;

    @GetMapping("/{userId}")
    public ProfileResponse getProfile(@PathVariable Long userId) {
        return userReader.getProfile(userId);
    }
}
