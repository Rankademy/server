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
    ;

    private final String url;
    private final HttpMethod method;
}
