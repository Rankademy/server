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
    @Column(length = 100)
    private String univName;

    private Email univMail;

    private boolean univVerified;

    private boolean inCollege;

    private int admissionYear;

    @Column(length = 100)
    private String major;


    @Builder
    public UnivInfo(String univName, Email univMail, boolean univVerified, boolean inCollege, int admissionYear, String major) {
        this.univName = univName;
        this.univMail = univMail;
        this.univVerified = univVerified;
        this.inCollege = inCollege;
        this.admissionYear = admissionYear;
        this.major = major;
    }

    static UnivInfo from(EnrollUnivRequest enrollUnivRequest) {
        return new UnivInfo(
                requireNonNull(enrollUnivRequest.univName()),
                new Email(enrollUnivRequest.univMail()),
                false,
                requireNonNull(enrollUnivRequest.inCollege()),
                enrollUnivRequest.admissionYear(),
                requireNonNull(enrollUnivRequest.major())
        );
    }

    UnivInfo update(EnrollUnivRequest enrollUnivRequest) {
        return new UnivInfo(
                requireNonNull(enrollUnivRequest.univName()),
                new Email(enrollUnivRequest.univMail()),
                this.univVerified, // 이전 값
                requireNonNull(enrollUnivRequest.inCollege()),
                enrollUnivRequest.admissionYear(),
                requireNonNull(enrollUnivRequest.major())
        );
    }

    public UnivInfo authenticate() {
        return UnivInfo.builder()
                .univName(requireNonNull(this.getUnivName()))
                .univMail(this.getUnivMail())
                .univVerified(true)
                .inCollege(this.isInCollege())
                .admissionYear(this.getAdmissionYear())
                .major(requireNonNull(this.getMajor()))
                .build();
    }
}