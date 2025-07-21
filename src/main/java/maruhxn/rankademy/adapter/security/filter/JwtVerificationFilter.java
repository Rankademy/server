package maruhxn.rankademy.adapter.security.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.jwt.JwtProvider;
import maruhxn.rankademy.adapter.security.model.RankademyAuthenticationToken;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static maruhxn.rankademy.adapter.security.Constants.ACCESS_TOKEN_HEADER;

@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader(ACCESS_TOKEN_HEADER);
        String accessToken = jwtProvider.getTokenFromBearer(bearerToken);

        if (!StringUtils.hasText(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtProvider.validate(accessToken);
        setAuthenticationToContext(accessToken);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return super.shouldNotFilter(request);
    }

    private void setAuthenticationToContext(String token) {
        RankademyUser rankademyUser = jwtProvider.getPrincipal(token);
        RankademyAuthenticationToken authentication = RankademyAuthenticationToken.authenticated(rankademyUser);
        SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
    }
}
