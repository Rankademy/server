package maruhxn.rankademy.domain.member.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("이미 사용 중인 유저명입니다: " + username);
    }
}
