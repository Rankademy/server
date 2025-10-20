package maruhxn.rankademy.domain.univ_certification_code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.user.Email;

import java.time.LocalDateTime;

@Entity
@Table(name = "univ_certification_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnivCertificationCode extends AbstractEntity {

    @Column(name = "code", nullable = false)
    public int code;

    @Column(name = "email", nullable = false)
    public Email email;

    @Column(name = "univ_name", nullable = false)
    public String univName;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "expired_at", nullable = false)
    public LocalDateTime expiredAt;

    public UnivCertificationCode(int code, Email email, String univName, Long userId, LocalDateTime expiredAt) {
        this.code = code;
        this.email = email;
        this.univName = univName;
        this.userId = userId;
        this.expiredAt = expiredAt;
    }

    public static UnivCertificationCode create(int code, Email email, String univName, Long userId, LocalDateTime expiredAt) {
        return new UnivCertificationCode(code, email, univName, userId, expiredAt);
    }
}
