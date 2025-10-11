package maruhxn.rankademy.adapter.webapi.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.application.user.provided.UserReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "유저 프로필 조회 API")
public class UserApi {

    private final UserReader userReader;

    @GetMapping("/{userId}")
    @Operation(
            summary = "유저 프로필 조회",
            description = "유저 ID로 유저의 기본 프로필 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    public ProfileResponse getProfile(
            @Parameter(description = "조회할 유저 ID", example = "1")
            @PathVariable Long userId
    ) {
        return userReader.getProfile(userId);
    }
}
