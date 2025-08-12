package maruhxn.rankademy.adapter.integration.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileProvider {

    Resource getFile(String storedFileName);

    void delete(String storedFileName);

    String upload(MultipartFile file);

    String buildImageUrl(String storedFileName);
}
