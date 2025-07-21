package maruhxn.rankademy.adapter.security.provider;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyAuthenticationToken;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.domain.user.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();

        RankademyUser rankademyUser = (RankademyUser) userDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(password, rankademyUser.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        return RankademyAuthenticationToken.authenticated(rankademyUser);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(RankademyAuthenticationToken.class);
    }
}
