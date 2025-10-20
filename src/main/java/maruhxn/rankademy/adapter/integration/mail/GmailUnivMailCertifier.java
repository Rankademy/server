package maruhxn.rankademy.adapter.integration.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.univ_certification_code.provided.UnivCertificationCodeManager;
import maruhxn.rankademy.application.user.required.UnivMailCertifier;
import maruhxn.rankademy.application.user.required.UnivMailValidator;
import maruhxn.rankademy.domain.univ_certification_code.UnivCertificationCode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class GmailUnivMailCertifier implements UnivMailCertifier {

    private final UnivMailValidator univMailValidator;
    private final JavaMailSender javaMailSender;
    private final UnivCertificationCodeManager univCertificationCodeManager;

    @Override
    public void sendCertifyMail(String email, String univName, int code) {
        if (!univMailValidator.isValid(univName, email)) {
            throw new IllegalArgumentException("이메일 정보가 올바르지 않습니다.");
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[Rankademy] 대학 메일 인증 코드");
            helper.setText(String.format("인증 코드: %d", code));
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송 실패: " + email, e);
        }
    }

    @Override
    public void certifyCode(String email, String univName, int code) {
        List<UnivCertificationCode> codes = univCertificationCodeManager.getUnivCertificationCodes(email, univName);

        LocalDateTime now = LocalDateTime.now();

        UnivCertificationCode univCertificationCode = codes.stream()
                .filter(c -> now.isBefore(c.expiredAt))
                .filter(c -> c.code == code)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("인증 코드가 일치하지 않습니다."));

        univCertificationCodeManager.delete(univCertificationCode);
    }
}
