package maruhxn.rankademy.adapter.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.user.required.UnivMailValidator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class JsonUnivMailValidator implements UnivMailValidator {

    private static final String UNIV_MAIL_FILE = "univ-mail.json";

    private Map<String, String> univInfo = Collections.emptyMap();

    public JsonUnivMailValidator() {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource(UNIV_MAIL_FILE);

        try (InputStream inputStream = resource.getInputStream()) {
            univInfo = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("대학 정보를 읽어오는 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public boolean isValid(String univName, String univMail) {
        if (!univInfo.containsKey(univName)) {
            throw new IllegalArgumentException("대학 정보가 존재하지 않습니다. 문의 바랍니다.");
        }

        String expectedDomain = univInfo.get(univName);
        String actualDomain = univMail.substring(univMail.indexOf("@") + 1);

        return expectedDomain.equals(actualDomain);
    }
}
