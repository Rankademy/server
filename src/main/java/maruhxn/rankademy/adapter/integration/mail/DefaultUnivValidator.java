package maruhxn.rankademy.adapter.integration.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.univ.required.UnivRepository;
import maruhxn.rankademy.application.user.required.UnivValidator;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultUnivValidator implements UnivValidator {

    private final UnivRepository univRepository;

    @Override
    public void validateUnivMail(String univName, String univMail) {
        String postfix = univMail.substring(univMail.indexOf("@") + 1);

        if (!univRepository.existsByUnivNameAndUnivMailPostfix(univName, postfix)) {
            throw new NoSuchElementException("대학 정보가 존재하지 않습니다. 문의 바랍니다.");
        }
    }
}
