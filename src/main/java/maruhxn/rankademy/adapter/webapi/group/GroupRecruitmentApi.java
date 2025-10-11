package maruhxn.rankademy.adapter.webapi.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupRecruitmentManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group Recruitment", description = "그룹 모집 상태 관리 API")
public class GroupRecruitmentApi {

    private final GroupRecruitmentManager groupRecruitmentManager;

    @PostMapping("/{groupId}/recruitment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "그룹 모집 시작",
            description = "그룹의 모집 상태를 활성화합니다."
    )
    @ApiResponse(responseCode = "201", description = "모집 시작 성공")
    public void startRecruitment(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable("groupId") Long groupId
    ) {
        groupRecruitmentManager.startRecruitment(groupId);
    }

    @DeleteMapping("/{groupId}/recruitment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "그룹 모집 종료",
            description = "그룹의 모집 상태를 비활성화합니다."
    )
    @ApiResponse(responseCode = "204", description = "모집 종료 성공")
    public void closeRecruitment(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable("groupId") Long groupId
    ) {
        groupRecruitmentManager.closeRecruitment(groupId);
    }
}
