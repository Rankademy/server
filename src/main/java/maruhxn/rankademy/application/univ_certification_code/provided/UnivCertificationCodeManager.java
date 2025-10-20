package maruhxn.rankademy.application.univ_certification_code.provided;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.univ_certification_code.required.UnivCertificationCodeRepository;
import maruhxn.rankademy.domain.univ_certification_code.UnivCertificationCode;
import maruhxn.rankademy.domain.user.Email;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnivCertificationCodeManager {

    private final UnivCertificationCodeRepository certificationCodeRepository;

    public void generateCode(Long userId, String univMail, String univName, int code) {
        // 특정 userId + univName만 삭제 (동시성 개선)
        certificationCodeRepository.deleteByUserIdAndUnivName(userId, univName);

        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
        certificationCodeRepository.save(
                UnivCertificationCode.create(
                        code,
                        new Email(univMail),
                        univName,
                        userId,
                        expiredAt
                )
        );
    }

    public List<UnivCertificationCode> getUnivCertificationCodes(String email, String univName) {
        return certificationCodeRepository.findByEmailAndUnivName(new Email(email), univName);
    }

    public void delete(UnivCertificationCode univCertificationCode) {
        certificationCodeRepository.delete(univCertificationCode);
    }
}
