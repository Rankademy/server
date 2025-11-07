package maruhxn.rankademy.application.user.required;

import maruhxn.rankademy.domain.univ_certification_code.UnivCertificationCode;

public interface UnivMailCertifier {

    void sendCertifyMail(String email, int code);

    UnivCertificationCode certifyCode(String email, int code);

}
