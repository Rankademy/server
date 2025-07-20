package maruhxn.rankademy.adapter.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import maruhxn.rankademy.adapter.security.dto.LoginRequest;
import maruhxn.rankademy.adapter.security.model.RankademyAuthenticationToken;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.server.MethodNotAllowedException;

import java.io.IOException;
import java.util.List;

public class RestLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestLoginFilter() {
        super(PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, "/api/v1/auth/login"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (!request.getMethod().equals(HttpMethod.POST.name())) {
            throw new MethodNotAllowedException(request.getMethod(), List.of(HttpMethod.POST));
        }

        LoginRequest loginRequest = objectMapper.readValue(request.getReader(), LoginRequest.class);
        RankademyAuthenticationToken unauthenticated = RankademyAuthenticationToken.unauthenticated(
                loginRequest.email(),
                loginRequest.password()
        );

        return this.getAuthenticationManager().authenticate(unauthenticated);
    }
}
