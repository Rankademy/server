package maruhxn.rankademy.adapter.security.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class RankademyAuthenticationToken extends AbstractAuthenticationToken {

    private Object principal;
    private Object credentials;

    public RankademyAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    public static RankademyAuthenticationToken authenticated(RankademyUser principal) {
        return new RankademyAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
