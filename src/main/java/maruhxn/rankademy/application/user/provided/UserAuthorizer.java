package maruhxn.rankademy.application.user.provided;

import jakarta.validation.Valid;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;

public interface UserAuthorizer {

    void sendUnivCertifyMail(Long userId, String univName, String email);

    User completeUnivAuthentication(Long userId, String email, int code);

    User completeRiotAuthentication(Long userId, @Valid RiotAuthRequest riotAuthRequest);

    User removeRiotAuthentication(Long userId);

}
