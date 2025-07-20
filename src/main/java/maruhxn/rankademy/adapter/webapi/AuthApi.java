package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApi {

    private final UserWriter userWriter;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Long register(
            @RequestBody @Valid UserRegisterRequest request
    ) {
        User user = userWriter.register(request);
        return user.getId();
    }
}
