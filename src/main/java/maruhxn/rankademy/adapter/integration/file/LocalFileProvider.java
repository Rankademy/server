package maruhxn.rankademy.adapter.integration.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Fallback;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 로컬에 파일을 CRD 할 수 있다
 */
@Slf4j
@Component
@Fallback
public class LocalFileProvider implements FileProvider {

    @Value("${server.url}")
    private String SERVER_URL;

    // Setter for testing purposes
    public void setSERVER_URL(String serverUrl) {
        this.SERVER_URL = serverUrl;
    }

    private final Path rootLocation;

    public LocalFileProvider(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 디렉토리 생성 실패: " + rootLocation, e);
        }
    }

    @Override
    public Resource getFile(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new NoSuchElementException("파일을 찾을 수 없습니다");
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("잘못된 파일 URL입니다.", ex);
        }
    }

    @Override
    public void delete(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            log.warn("파일 삭제 실패: {}", storedFileName, ex);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        // 원본 파일명에서 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // UUID 및 타임스탬프로 충돌 방지, 확장자 포함
        String filename = System.currentTimeMillis()
                + "-" + UUID.randomUUID()
                + extension;

        try (InputStream input = file.getInputStream()) {
            Path destination = rootLocation.resolve(filename);
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException ex) {
            throw new IllegalStateException("파일 저장 실패: " + filename, ex);
        }
    }

    @Override
    public String buildImageUrl(String storedFileName) {
        return String.format("%s/api/v1/images?imageName=%s", SERVER_URL, storedFileName);
    }
}
