package maruhxn.rankademy.adapter.integration.mail;

import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.user.required.UnivMailCertifier;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Fallback
public class DummyUnivMailCertifier implements UnivMailCertifier {

    @Override
    public void sendCertifyMail(String email, int code) {
        log.info("email: {}, code: {}", email, code);
    }

    @Override
    public void certifyCode(String email, String univName, int code) {
        log.info("email: {}, univName: {}, code: {}", email, univName, code);
    }
}
