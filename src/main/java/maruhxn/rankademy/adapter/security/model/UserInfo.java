package maruhxn.rankademy.adapter.security.model;

import lombok.Builder;
import maruhxn.rankademy.domain.user.User;

@Builder
public record UserInfo(
        Long id,
        String username,
        String email,
        boolean isAuthorized,
        String role
) {
    public static UserInfo from(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail().address())
                .isAuthorized(user.isAuthorized())
                .role(user.getRole().name())
                .build();
    }
}
