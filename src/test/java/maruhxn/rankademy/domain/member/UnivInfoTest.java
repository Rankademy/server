package maruhxn.rankademy.domain.member;

import maruhxn.rankademy.domain.member.dto.CertifyUnivRequest;
import org.junit.jupiter.api.Test;

import static maruhxn.rankademy.domain.member.MemberFixture.createCertifyUnivRequest;
import static org.assertj.core.api.Assertions.assertThat;

class UnivInfoTest {

    @Test
    void create() {
        CertifyUnivRequest request = createCertifyUnivRequest();
        UnivInfo univInfo = UnivInfo.from(request);

        assertThat(univInfo.univName()).isEqualTo(request.univName());
        assertThat(univInfo.univMail().address()).isEqualTo(request.univMail());
    }

}