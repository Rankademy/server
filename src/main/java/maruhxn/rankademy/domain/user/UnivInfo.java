package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;

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
        // 학교명과 학교메일 형식이 매칭되는지 확인 필요
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
                .univName(requireNonNull(this.univName()))
                .univMail(this.univMail())
                .univVerified(true)
                .inCollege(this.inCollege())
                .admissionYear(this.admissionYear())
                .major(requireNonNull(this.major()))
                .build();
    }
}
