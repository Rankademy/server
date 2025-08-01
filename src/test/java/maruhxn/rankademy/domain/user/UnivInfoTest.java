package maruhxn.rankademy.domain.user;

import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import org.junit.jupiter.api.Test;

import static maruhxn.rankademy.domain.user.UserFixture.createEnrollUnivRequest;
import static org.assertj.core.api.Assertions.assertThat;

class UnivInfoTest {

    @Test
    void create() {
        EnrollUnivRequest request = createEnrollUnivRequest();
        UnivInfo univInfo = UnivInfo.from(request);

        assertThat(univInfo.getUnivName()).isEqualTo(request.univName());
        assertThat(univInfo.getUnivMail().address()).isEqualTo(request.univMail());
    }

}