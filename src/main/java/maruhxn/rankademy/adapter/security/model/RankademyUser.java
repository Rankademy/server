package maruhxn.rankademy.adapter.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public record RankademyUser(
        UserInfo userInfo,
        Map<String, Object> attributes
) implements UserDetails, OAuth2User {

    public static RankademyUser from(UserInfo userInfo) {
        return new RankademyUser(userInfo, new HashMap<>());
    }

    public static RankademyUser from(UserInfo userInfo, Map<String, Object> attributes) {
        return new RankademyUser(userInfo, attributes);
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

    @Override
    public String getName() {
        return userInfo.username();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
}
