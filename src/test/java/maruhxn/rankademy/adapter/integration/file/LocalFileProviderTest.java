package maruhxn.rankademy.adapter.integration.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LocalFileProvider 테스트")
class LocalFileProviderTest {

    @TempDir
    Path tempDir;

    private LocalFileProvider localFileProvider;

    @BeforeEach
    void setUp() {
        localFileProvider = new LocalFileProvider(tempDir.toString());
    }

    @Test
    @DisplayName("파일 업로드 테스트")
    void upload() throws IOException {
        // given
        String originalFileName = "test-image.png";
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                originalFileName,
                "image/png",
                "test data".getBytes()
        );

        // when
        String storedFileName = localFileProvider.upload(multipartFile);

        // then
        assertThat(storedFileName).isNotNull();
        assertThat(storedFileName).doesNotContain(originalFileName); // Check if the name is randomized
        assertThat(Files.exists(tempDir.resolve(storedFileName))).isTrue();
    }

    @Test
    @DisplayName("파일 조회 테스트")
    void getFile() throws IOException {
        // given
        String content = "hello world";
        Path file = tempDir.resolve("test.txt");
        Files.write(file, content.getBytes());

        // when
        Resource resource = localFileProvider.getFile("test.txt");

        // then
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
        assertThat(new String(resource.getInputStream().readAllBytes())).isEqualTo(content);
    }

    @Test
    @DisplayName("존재하지 않는 파일 조회 시 예외 발생 테스트")
    void getFile_whenFileNotExists_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> {
            localFileProvider.getFile("non-existent-file.txt");
        }).isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessage("파일을 찾을 수 없습니다");
    }


    @Test
    @DisplayName("파일 삭제 테스트")
    void delete() throws IOException {
        // given
        Path file = tempDir.resolve("delete-me.txt");
        Files.createFile(file);
        assertThat(Files.exists(file)).isTrue();

        // when
        localFileProvider.delete("delete-me.txt");

        // then
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    @DisplayName("이미지 URL 빌드 테스트")
    void buildImageUrl() {
        // given
        localFileProvider.setSERVER_URL("http://localhost:8080");
        String storedFileName = "test-image.png";

        // when
        String imageUrl = localFileProvider.buildImageUrl(storedFileName);

        // then
        assertThat(imageUrl).isEqualTo("http://localhost:8080/api/v1/files?fileName=test-image.png");
    }
}