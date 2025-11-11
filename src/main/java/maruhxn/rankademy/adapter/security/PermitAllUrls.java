package maruhxn.rankademy.adapter.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import static org.springframework.http.HttpMethod.GET;

@Getter
@RequiredArgsConstructor
public enum PermitAllUrls {

    CSS("/css/**", GET),
    IMAGES("/images/**", GET),
    JS("/js/**", GET),
    FAVICON("/favicon.*", GET),
    ICON("/*/icon-*", GET),
    REFRESH("/api/v1/auth/refresh", GET),
    UNIV_RANKING("/api/v1/rankings/univ", GET),
    USER_RANKING("/api/v1/rankings/users", GET),
    UNIV_STUDENT_RANKING("/api/v1/rankings/univ/{univName}", GET),
    GROUP_RANKING("/api/v1/rankings/univ/{univName}/groups*", GET),
    GROUP_DETAIL("/api/v1/groups/{groupId}", GET),
    GROUP_RECENT_COMPETITIONS("/api/v1/groups/{groupId}/recent-competitions", GET),
    GROUP_MEMBERS("/api/v1/groups/{groupId}/members", GET),
    GROUP_RECRUITMENT_POST_LIST("/api/v1/groups/posts", GET),
    GROUP_RECRUITMENT_POST_DETAIL("/api/v1/groups/{groupId}/post", GET),
    GET_IMAGE("/api/v1/files**", GET),
    SWAGGER_UI("/docs/swagger-ui.html", GET),
    SWAGGER_UI_RESOURCES("/docs/swagger-ui/**", GET),
    SWAGGER_API_DOCS("/docs/api-docs/**", GET),
    SCRIM_TEAM_LIST("/api/v1/scrim-teams**", GET),
    SCRIM_TEAM_DETAILS("/api/v1/scrim-teams/{scrimTeamId}", GET),
    GROUP_COMPETITIONS_LIST("/api/v1/competitions/groups/{groupId}**", GET),
    GET_USER_PROFILE("/api/v1/users/{userId}", GET),
    ;

    private final String url;
    private final HttpMethod method;
}
