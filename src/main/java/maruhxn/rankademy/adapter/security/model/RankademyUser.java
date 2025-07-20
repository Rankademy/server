package maruhxn.rankademy.adapter.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record RankademyUser(
        UserInfo userInfo
) implements UserDetails {

    public static RankademyUser from(UserInfo userInfo) {
        return new RankademyUser(userInfo);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(userInfo.role());
        authorities.add(simpleGrantedAuthority);
        return authorities;
    }

    @Override
    public String getPassword() {
        return userInfo.password();
    }

    @Override
    public String getUsername() {
        return userInfo.email();
    }

    public Long getId() {
        return userInfo.id();
    }

    public String getEmail() {
        return userInfo.email();
    }

    public String getNickname() {
        return userInfo.username();
    }
}
