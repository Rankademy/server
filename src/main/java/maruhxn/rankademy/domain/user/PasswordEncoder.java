package maruhxn.rankademy.domain.user;

public interface PasswordEncoder {

    String encode(String password);

    boolean matches(String password, String passwordHash);

}
