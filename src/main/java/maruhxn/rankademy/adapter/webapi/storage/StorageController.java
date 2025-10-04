package maruhxn.rankademy.adapter.webapi.storage;

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
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final PreSignedUrlIssuer preSignedUrlIssuer;

    @PostMapping("/pre-signed/batch")
    public List<PresignResponse> issueFilePutUrls(
            @RequestBody @Valid List<IssuePreSignedUrlRequest> request
    ) {
        return preSignedUrlIssuer.batchIssuePutUrls(request);
    }
}
