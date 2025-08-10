package maruhxn.rankademy.adapter.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Getter
@RequiredArgsConstructor
public enum PermitAllUrls {

    CSS("/css/**", GET),
    IMAGES("/images/**", GET),
    JS("/js/**", GET),
    FAVICON("/favicon.*", GET),
    ICON("/*/icon-*", GET),
    REGISTER("/api/v1/auth/register", POST),
    REFRESH("/api/v1/auth/refresh", GET),
    UNIV_RANKING("/api/v1/rankings/univ", GET),
    UNIV_STUDENT_RANKING("/api/v1/rankings/univ/{univName}", GET),
    GROUP_RANKING("/api/v1/groups*", GET),
    GROUP_DETAIL("/api/v1/groups/{groupId}", GET),
    GROUP_RECENT_COMPETITIONS("/api/v1/groups/{groupId}/recent-competitions", GET),
    GROUP_MEMBERS("/api/v1/groups/{groupId}/members", GET),
    GROUP_RECRUITMENT_POST_LIST("/api/v1/groups/posts", GET),
    GROUP_RECRUITMENT_POST_DETAIL("/api/v1/groups/{groupId}/post", GET),
    ;

    private final String url;
    private final HttpMethod method;
}
