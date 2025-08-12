package maruhxn.rankademy.adapter.webapi;

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
public class FileApi {

    private final FileProvider fileProvider;

    /**
     * 이미지 조회
     */
    @GetMapping
    public ResponseEntity<Resource> getImage(@RequestParam("imageName") String imageName) {
        Resource file = fileProvider.getFile(imageName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageName + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    /**
     * 파일 업로드
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadFile(@RequestPart("file") MultipartFile file) {
        String storedFileName = fileProvider.upload(file);

        return fileProvider.buildImageUrl(storedFileName);
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(@RequestParam("imageName") String imageName) {
        fileProvider.delete(imageName);
    }
}