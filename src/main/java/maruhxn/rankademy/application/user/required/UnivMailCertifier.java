package maruhxn.rankademy.application.user.required;

public interface UnivMailCertifier {

    void sendCertifyMail(String email, String univName, int code);

    void certifyCode(String email, String univName, int code);

}
