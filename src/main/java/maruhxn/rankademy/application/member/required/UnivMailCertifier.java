package maruhxn.rankademy.application.member.required;

public interface UnivMailCertifier {

    void sendCertifyMail(String email, String univName, boolean inCollege);

    void certifyCode(String email, String univName, int code);

}
