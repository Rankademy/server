package maruhxn.rankademy.adapter.webapi.file;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.integration.file.FileProvider;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Hidden
//@Tag(name = "Files", description = "파일 업로드 및 조회 API")
public class FileApi {

    private final FileProvider fileProvider;

    /**
     * 이미지 조회
     */
    @Deprecated
    @GetMapping
    @Operation(
            summary = "이미지 조회",
            description = "저장된 파일명을 이용해 이미지를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "이미지 조회 성공")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "조회할 파일명", example = "sample.jpg")
            @RequestParam("fileName") String fileName
    ) {
        Resource file = fileProvider.getFile(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    /**
     * 파일 업로드
     */
    @Deprecated
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "파일 업로드",
            description = "멀티파트 파일을 업로드하고 접근 가능한 URL을 반환합니다."
    )
    @ApiResponse(responseCode = "201", description = "파일 업로드 성공")
    public String uploadFile(
            @Parameter(description = "업로드할 파일")
            @RequestPart("file") MultipartFile file
    ) {
        String storedFileName = fileProvider.upload(file);

        return fileProvider.buildImageUrl(storedFileName);
    }

    /**
     * 파일 삭제
     */
    @Deprecated
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "파일 삭제",
            description = "저장된 파일을 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "파일 삭제 성공")
    public void deleteFile(
            @Parameter(description = "삭제할 파일명", example = "sample.jpg")
            @RequestParam("fileName") String fileName
    ) {
        fileProvider.delete(fileName);
    }
}
