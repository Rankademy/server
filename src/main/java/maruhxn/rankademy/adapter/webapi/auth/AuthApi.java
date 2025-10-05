package maruhxn.rankademy.adapter.webapi.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증 및 토큰 관리 API")
public class AuthApi {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원 가입",
            description = "신규 사용자 정보를 등록하고 사용자 ID를 반환합니다."
    )
    @ApiResponse(responseCode = "201", description = "회원 가입 성공")
    public Long register(
            @RequestBody @Valid UserRegisterRequest request
    ) {
        User user = userWriter.registerOrSetPassword(request);
        return user.getId();
    }

    @GetMapping("/refresh")
    @Operation(
            summary = "액세스 토큰 재발급",
            description = "유효한 리프레시 토큰으로 새로운 액세스/리프레시 토큰을 발급합니다."
    )
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    public TokenDto refresh(
            @Parameter(description = "Bearer 형식의 리프레시 토큰", example = "Bearer eyJhbGciOi...")
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
