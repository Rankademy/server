package maruhxn.rankademy.adapter.integration;

import maruhxn.rankademy.application.member.required.UnivMailCertifier;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Component
@Fallback
public class DummyUnivMailCertifier implements UnivMailCertifier {

    @Override
    public void sendCertifyMail(String email, String univName, boolean inCollege) {

    }

    @Override
    public void certifyCode(String email, String univName, int code) {
    }
}
