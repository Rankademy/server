package maruhxn.rankademy.application.user.required;

import maruhxn.rankademy.application.user.dto.MyProfileResponse;
import maruhxn.rankademy.application.user.dto.ProfileResponse;

import java.util.Optional;

public interface UserQueryRepository {

    Optional<ProfileResponse> getProfile(Long userId);

    Optional<MyProfileResponse> getMyProfile(Long userId);
}
