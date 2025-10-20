package maruhxn.rankademy.adapter.integration.mail;

import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.domain.user.Email;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Component
@Fallback
public class DummyEmailSender implements EmailSender {
    @Override
    public void send(Email email, String subject, String body) {
        System.out.println("Dummy email send: " + email);
    }
}
