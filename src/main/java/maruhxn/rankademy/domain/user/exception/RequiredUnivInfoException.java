package maruhxn.rankademy.domain.user.exception;

public class RequiredUnivInfoException extends RuntimeException {
    public RequiredUnivInfoException() {
        super("학교 정보를 등록해주세요.");
    }
}
