package maruhxn.rankademy.application.user.provided;

import jakarta.validation.Valid;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;

/**
 * 회원 등록과 관련된 기능 제공
 */
public interface UserWriter {

    User registerOrSetPassword(@Valid UserRegisterRequest registerRequest);

    User enrollUnivInfo(Long userId, @Valid EnrollUnivRequest enrollUnivRequest);

    User removeUnivInfo(Long userId);

    User updateProfile(Long userId, @Valid ProfileUpdateRequest updateProfileRequest);

    void withdraw(Long userId);

    User oauth2Register(UserOAuth2CreateRequest userOAuth2CreateRequest);
}
