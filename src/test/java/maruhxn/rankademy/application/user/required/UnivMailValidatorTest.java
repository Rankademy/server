package maruhxn.rankademy.application.user.required;

import maruhxn.rankademy.adapter.integration.mail.JsonUnivMailValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnivMailValidatorTest {

    private final UnivMailValidator univMailValidator = new JsonUnivMailValidator();

    @Test
    @DisplayName("정상적인 대학 이름과 이메일이 주어지면, 유효성 검사를 통과한다.")
    void isValid() {
        // given
        String univName = "서울과학기술대학교";
        String univMail = "tester@seoultech.ac.kr";

        // when
        boolean result = univMailValidator.isValid(univName, univMail);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("대학 이름은 존재하지만, 이메일 도메인이 일치하지 않으면 유효성 검사에 실패한다.")
    void isInvalidWhenDomainDoesNotMatch() {
        // given
        String univName = "서울과학기술대학교";
        String univMail = "tester@wrong.ac.kr";

        // when
        boolean result = univMailValidator.isValid(univName, univMail);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 대학 이름이 주어지면, 유효성 검사에 실패한다.")
    void isInvalidWhenUnivNameDoesNotExist() {
        // given
        String univName = "없는대학교";
        String univMail = "tester@test.ac.kr";

        // when
        assertThatThrownBy(() -> univMailValidator.isValid(univName, univMail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("공주대학교처럼 메일 도메인에 서브도메인이 포함되어도, 유효성 검사를 통과한다.")
    void isValidWithSubdomain() {
        // "공주대학교": "smail.kongju.ac.kr",
        // given
        String univName = "공주대학교";
        String univMail = "tester@smail.kongju.ac.kr";

        // when
        boolean result = univMailValidator.isValid(univName, univMail);

        // then
        assertThat(result).isTrue();
    }
}