package maruhxn.rankademy.adapter.webapi.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.SearchedUserResponse;
import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.application.user.provided.UserReader;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "유저 프로필 조회 API")
public class UserApi {

    private final UserReader userReader;

    @GetMapping("/search")
    @Operation(
            summary = "전체 유저 검색",
            description = "userNameKey로 유저를 검색합니다. (최대 4개)"
    )
    @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    public List<SearchedUserResponse> searchUsers(
            @Parameter(description = "유저 소환사명 키", example = "니카")
            @RequestParam(value = "userNameKey") String userNameKey
    ) {
        return userReader.searchUsers(userNameKey);
    }

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
