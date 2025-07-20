package maruhxn.rankademy.application.user.required;


import maruhxn.rankademy.domain.user.Email;

/**
 * 이메일을 발송한다
 */
public interface EmailSender {
    void send(Email email, String subject, String body);
}
