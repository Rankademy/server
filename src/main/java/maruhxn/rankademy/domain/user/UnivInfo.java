package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;

import static java.util.Objects.requireNonNull;

@Embeddable
@Getter
@NoArgsConstructor
public class UnivInfo {
    @Column(name = "univ_name", length = 100)
    private String univName;

    private Email univMail;

    private Integer admissionYear;

    @Column(length = 100)
    private String major;

    @Builder
    public UnivInfo(String univName, Email univMail, Integer admissionYear, String major) {
        this.univName = univName;
        this.univMail = univMail;
        this.admissionYear = admissionYear;
        this.major = major;
    }

    static UnivInfo empty() {
        return new UnivInfo(null, null, null, "미설정");
    }

    static UnivInfo from(EnrollUnivRequest enrollUnivRequest) {
        return new UnivInfo(
                requireNonNull(enrollUnivRequest.univName()),
                new Email(enrollUnivRequest.univMail()),
                null,
                null
        );
    }

    public boolean isAuthorized() {
        return univMail != null && univName != null;
    }

    public void updateAdmissionYear(Integer admissionYear) {
        this.admissionYear = admissionYear;
    }

    public void updateMajor(String major) {
        this.major = major;
    }
}