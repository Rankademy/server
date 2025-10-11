package maruhxn.rankademy.adapter.webapi.storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.storage.PreSignedUrlIssuer;
import maruhxn.rankademy.adapter.storage.dto.IssuePreSignedUrlRequest;
import maruhxn.rankademy.adapter.storage.dto.PresignResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "스토리지 업로드 지원 API")
public class StorageApi {

    private final PreSignedUrlIssuer preSignedUrlIssuer;

    @PostMapping("/pre-signed/batch")
    @Operation(
            summary = "멀티 파트 업로드용 Pre-Signed URL 발급",
            description = "파일 업로드를 위한 Pre-Signed URL을 일괄 발급합니다."
    )
    @ApiResponse(responseCode = "200", description = "Pre-Signed URL 발급 성공")
    public List<PresignResponse> issueFilePutUrls(
            @RequestBody @Valid List<IssuePreSignedUrlRequest> request
    ) {
        return preSignedUrlIssuer.batchIssuePutUrls(request);
    }
}
