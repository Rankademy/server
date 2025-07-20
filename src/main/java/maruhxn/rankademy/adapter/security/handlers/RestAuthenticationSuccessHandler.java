package maruhxn.rankademy.adapter.security.handlers;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.dto.TokenDto;
import maruhxn.rankademy.adapter.security.jwt.JwtProvider;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.user.User;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;
    private final UserReader userReader;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        RankademyUser principal = (RankademyUser) authentication.getPrincipal();
        User user = userReader.find(principal.getId());
        TokenDto tokenDto = jwtProvider.createJwt(principal);
        user.addRefreshToken(tokenDto.refreshToken());
//        jwtService.setCookie(tokenDto, response);

        objectMapper.writeValue(response.getWriter(), tokenDto);

        this.clearAuthenticationAttributes(request);
    }

    // 성공했으니 spring security에서 발생했던 예외들을 삭제해줌
    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
