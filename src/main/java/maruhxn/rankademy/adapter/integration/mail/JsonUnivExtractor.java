package maruhxn.rankademy.adapter.integration.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.univ.required.UnivRepository;
import maruhxn.rankademy.application.user.required.UnivExtractor;
import maruhxn.rankademy.domain.univ.Univ;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUnivExtractor implements UnivExtractor {

    private final UnivRepository univRepository;

    @Override
    public String extractUnivNameFromMail(String univMail) {
        String actualDomain = univMail.substring(univMail.indexOf("@") + 1);

        Univ univ = univRepository.findByUnivMailPostfix(actualDomain)
                .orElseThrow(() -> new NoSuchElementException("대학 정보가 존재하지 않습니다. 문의 바랍니다."));

        return univ.getUnivName();
    }
}
