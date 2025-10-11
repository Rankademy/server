package maruhxn.rankademy.adapter.security.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.jwt.JwtProvider;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static maruhxn.rankademy.adapter.security.Constants.REFRESH_TOKEN_HEADER;

@Component
@RequiredArgsConstructor
public class RestLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserReader userReader;

    @Override
    @Transactional
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String bearerRefreshToken = request.getHeader(REFRESH_TOKEN_HEADER);
        String refreshToken = jwtProvider.getTokenFromBearer(bearerRefreshToken);
        String email = jwtProvider.getEmail(refreshToken);
        User user = userReader.getByEmail(email);
        user.invalidateAllTokens();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
