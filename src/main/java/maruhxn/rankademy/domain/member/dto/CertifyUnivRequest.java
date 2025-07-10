package maruhxn.rankademy.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Year;

public record CertifyUnivRequest(
        @NotEmpty
        @Size(max = 100)
        String univName,

        @Email
        String univMail,

        @NotNull
        Boolean univVerified,

        @NotNull
        Boolean inCollege,

        @NotNull
        int admissionYear,

        @NotEmpty
        @Size(min = 1, max = 100)
        String major
) {
    public CertifyUnivRequest {
        int currentYear = Year.now().getValue();
        if (admissionYear > currentYear) {
            throw new IllegalArgumentException("입학년도가 올바르지 않습니다: " + admissionYear);
        }
    }
}
