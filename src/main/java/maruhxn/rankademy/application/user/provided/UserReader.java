package maruhxn.rankademy.application.user.provided;

import maruhxn.rankademy.domain.user.User;

public interface UserReader {

    User find(Long userId);
}
