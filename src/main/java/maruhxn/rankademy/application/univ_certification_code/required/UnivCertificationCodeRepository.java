package maruhxn.rankademy.application.univ_certification_code.required;

import maruhxn.rankademy.domain.univ_certification_code.UnivCertificationCode;
import maruhxn.rankademy.domain.user.Email;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface UnivCertificationCodeRepository extends Repository<UnivCertificationCode, Long> {
    UnivCertificationCode save(UnivCertificationCode univCertificationCode);

    List<UnivCertificationCode> findByEmail(Email email);

    void delete(UnivCertificationCode univCertificationCode);

    void deleteByUserIdAndUnivName(Long userId, String univName);
}
