package maruhxn.rankademy.application.user.required;

import maruhxn.rankademy.adapter.webapi.dto.SearchedUserResponse;
import maruhxn.rankademy.application.user.dto.MyProfileResponse;
import maruhxn.rankademy.application.user.dto.ProfileResponse;

import java.util.List;
import java.util.Optional;

public interface UserQueryRepository {

    Optional<ProfileResponse> getProfile(Long userId);

    Optional<MyProfileResponse> getMyProfile(Long userId);

    List<SearchedUserResponse> searchUsersByKey(String userNameKey);
}
