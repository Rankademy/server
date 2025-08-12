package maruhxn.rankademy.adapter.webapi;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.integration.file.FileProvider;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static maruhxn.rankademy.domain.user.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
@DisplayName("FileApi 테스트")
class FileApiTest {

    static final String BASE_URL = "/api/v1/files";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    FileProvider fileProvider;

    @Test
    void uploadAndGetAndDeleteFile() throws Exception {
        // given
        User user = registerUser();
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));
        when(fileProvider.upload(any(MockMultipartFile.class))).thenReturn("test.jpg");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy-jpeg-content".getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        // 1. Upload
        MvcTestResult uploadResult = mvcTester.post().uri(BASE_URL)
                .with(user(mockUser))
                .multipart()
                .file(file)
                .exchange();

        assertThat(uploadResult).hasStatus(HttpStatus.CREATED);
        String fileUrl = uploadResult.getResponse().getContentAsString();
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        assertThat(fileUrl).contains(fileName);

        // 2. Get
        MvcTestResult getResult = mvcTester.get().uri(BASE_URL)
                .param("fileName", fileName)
                .exchange();

        assertThat(getResult).hasStatusOk();
        assertThat(getResult.getResponse().getHeader("Content-Type")).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        assertThat(getResult.getResponse().getHeader("Content-Disposition")).isEqualTo("inline; filename=\"" + fileName + "\"");


        // 3. Delete
        MvcTestResult deleteResult = mvcTester.delete().uri(BASE_URL)
                .param("fileName", fileName)
                .with(user(mockUser))
                .exchange();

        assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);
    }

    private User registerUser() {
        User user = userRepository.save(createUser());
        em.flush();
        em.clear();
        return user;
    }
}