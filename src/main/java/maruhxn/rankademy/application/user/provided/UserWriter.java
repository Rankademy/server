package maruhxn.rankademy.application.user.provided;

import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;

/**
 * 회원 등록과 관련된 기능 제공
 */
public interface UserWriter {

//    User enrollUnivInfo(Long userId, EnrollUnivRequest enrollUnivRequest);

    User removeUnivInfo(Long userId);

    User updateProfile(Long userId, ProfileUpdateRequest updateProfileRequest);

    void withdraw(Long userId);

    User oauth2Register(UserOAuth2CreateRequest userOAuth2CreateRequest);
}
