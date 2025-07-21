package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.dto.TokenDto;
import maruhxn.rankademy.adapter.security.jwt.JwtProvider;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static maruhxn.rankademy.adapter.security.Constants.REFRESH_TOKEN_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApi {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Long register(
            @RequestBody @Valid UserRegisterRequest request
    ) {
        User user = userWriter.registerOrSetPassword(request);
        return user.getId();
    }

    @GetMapping("/refresh")
    public TokenDto refresh(
            @RequestHeader(value = REFRESH_TOKEN_HEADER) String bearerRefreshToken
    ) {
        String refreshToken = jwtProvider.getTokenFromBearer(bearerRefreshToken);
        jwtProvider.validate(refreshToken);
        User user = userReader.getByRefreshToken(refreshToken);
        TokenDto tokenDto = jwtProvider.createJwt(RankademyUser.from(UserInfo.from(user)));
        user.rotateRefreshToken(refreshToken, tokenDto.refreshToken());
        return tokenDto;
    }
}
