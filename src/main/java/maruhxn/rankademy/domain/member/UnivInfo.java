package maruhxn.rankademy.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import maruhxn.rankademy.domain.member.dto.CertifyUnivRequest;

import static java.util.Objects.requireNonNull;

@Embeddable
public record UnivInfo(
        @Column(length = 100)
        String univName,

        Email univMail,

        boolean univVerified,

        boolean inCollege,

        int admissionYear,

        @Column(length = 100)
        String major
) {

    @Builder
    public UnivInfo {
    }

    static UnivInfo from(CertifyUnivRequest certifyUnivRequest) {
        return UnivInfo.builder()
                .univName(requireNonNull(certifyUnivRequest.univName()))
                .univMail(new Email(certifyUnivRequest.univMail()))
                .univVerified(requireNonNull(certifyUnivRequest.univVerified()))
                .inCollege(requireNonNull(certifyUnivRequest.inCollege()))
                .admissionYear(certifyUnivRequest.admissionYear())
                .major(requireNonNull(certifyUnivRequest.major()))
                .build();
    }
}
