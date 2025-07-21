package maruhxn.rankademy.adapter.security.service;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.adapter.security.model.oauth2.OAuth2ProviderUser;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankademyOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserReader userReader;
    private final UserWriter userWriter;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest); // 인가 서버와 통신해서 사용자 정보 조회

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        OAuth2ProviderUser oAuth2ProviderUser = this.getOAuth2ProviderUser(clientRegistration, oAuth2User);

        Optional<User> optionalUser = userReader
                .findByProviderAndOauthId(oAuth2ProviderUser.getProvider(), oAuth2ProviderUser.getProviderId());

        User user = optionalUser
                .orElseGet(() ->
                        userReader.findByEmail(oAuth2ProviderUser.getEmail())
                                .map(existingUser -> {
                                    existingUser.addOAuthAccount(oAuth2ProviderUser.getProvider(), oAuth2ProviderUser.getProviderId());
                                    return existingUser;
                                })
                                .orElseGet(() -> userWriter.oauth2Register(
                                        new UserOAuth2CreateRequest(
                                                oAuth2ProviderUser.getEmail(),
                                                oAuth2ProviderUser.getUsername(),
                                                oAuth2ProviderUser.getProvider(),
                                                oAuth2ProviderUser.getProviderId()
                                        )
                                ))
                );

        UserInfo userInfo = UserInfo.from(user);
        return RankademyUser.from(userInfo, oAuth2ProviderUser.getAttributes());
    }

    private OAuth2ProviderUser getOAuth2ProviderUser(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        String registrationId = clientRegistration.getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        return OAuth2ProviderUser.create(attributes, registrationId);
    }
}
