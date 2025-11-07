package maruhxn.rankademy.adapter.webapi.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.user.dto.MyProfileResponse;
import maruhxn.rankademy.application.user.provided.UserAuthorizer;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
@Tag(name = "Profile", description = "내 프로필 및 인증 관리 API")
public class ProfileApi {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final UserAuthorizer userAuthorizer;

    @GetMapping
    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 상세 프로필 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    public MyProfileResponse getMyProfile(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        return userReader.getMyProfile(rankademyUser.getId());
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "프로필 수정",
            description = "사용자 프로필 정보를 수정합니다."
    )
    @ApiResponse(responseCode = "204", description = "프로필 수정 성공")
    public void updateProfile(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid ProfileUpdateRequest profileUpdateRequest
    ) {
        userWriter.updateProfile(rankademyUser.getId(), profileUpdateRequest);
    }

//    @PostMapping("/univ")
//    @Operation(
//            summary = "대학교 정보 등록",
//            description = "대학교 인증 정보를 등록합니다."
//    )
//    @ApiResponse(responseCode = "200", description = "대학교 정보 등록 성공")
//    public void enrollUnivInfo(
//            @AuthenticationPrincipal RankademyUser rankademyUser,
//            @RequestBody @Valid EnrollUnivRequest enrollUnivRequest
//    ) {
//        userWriter.enrollUnivInfo(rankademyUser.getId(), enrollUnivRequest);
//    }

    @DeleteMapping("/univ")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "대학교 정보 삭제",
            description = "등록된 대학교 정보를 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "대학교 정보 삭제 성공")
    public void removeUnivInfo(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        userWriter.removeUnivInfo(rankademyUser.getId());
    }

    @PostMapping("/univ-email/send")
    @Operation(
            summary = "대학교 인증 메일 발송",
            description = "학교 인증을 위한 이메일을 발송합니다."
    )
    @ApiResponse(responseCode = "200", description = "인증 메일 발송 성공")
    public void sendCertifyUnivMail(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestParam(name = "univName") String univName,
            @RequestParam(name = "email") String email
    ) {
        userAuthorizer.sendUnivCertifyMail(rankademyUser.getId(), univName, email);
    }

    @PostMapping("/univ-email/certify")
    @Operation(
            summary = "대학교 인증 완료",
            description = "이메일로 전달된 인증 코드를 검증해 대학 인증을 완료합니다."
    )
    @ApiResponse(responseCode = "200", description = "대학교 인증 성공")
    public void certifyUnivMail(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @Parameter(description = "코드가 발급된 이메일", example = "test@seoultech.ac.kr")
            @RequestParam(name = "email") String email,
            @Parameter(description = "메일로 발급된 인증 코드", example = "123456")
            @RequestParam(name = "code", required = true) int code
    ) {
        userAuthorizer.completeUnivAuthentication(rankademyUser.getId(), email, code);
    }

    @PostMapping("/rso")
    @Operation(
            summary = "라이엇 인증 완료",
            description = "라이엇 계정 인증을 등록합니다."
    )
    @ApiResponse(responseCode = "200", description = "라이엇 인증 성공")
    public void rso(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid RiotAuthRequest riotAuthRequest
    ) {
        userAuthorizer.completeRiotAuthentication(rankademyUser.getId(), riotAuthRequest);
    }

    @DeleteMapping("/rso")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "라이엇 인증 해제",
            description = "등록된 라이엇 인증을 제거합니다."
    )
    @ApiResponse(responseCode = "204", description = "라이엇 인증 해제 성공")
    public void removeRso(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        userAuthorizer.removeRiotAuthentication(rankademyUser.getId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "회원 탈퇴",
            description = "사용자 계정을 탈퇴 처리합니다."
    )
    @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공")
    public void withdraw(@AuthenticationPrincipal RankademyUser rankademyUser) {
        userWriter.withdraw(rankademyUser.getId());
    }
}
