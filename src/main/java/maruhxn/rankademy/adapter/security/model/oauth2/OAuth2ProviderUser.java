package maruhxn.rankademy.adapter.security.model.oauth2;

import maruhxn.rankademy.domain.user.OAuth2Provider;

import java.util.HashMap;
import java.util.Map;

public abstract class OAuth2ProviderUser {

    private static final String GOOGLE_REGISTRANT_ID = "google";
    private static final String NAVER_REGISTRANT_ID = "naver";
    private static final String KAKAO_REGISTRANT_ID = "kakao";

    private String provider;
    private final Map<String, Object> attributes;

    protected OAuth2ProviderUser(Map<String, Object> attributes, String registrationId) {
        this.attributes = attributes;
        this.provider = registrationId;
    }

    // 팩토리 메서드
    public static OAuth2ProviderUser create(Map<String, Object> attributes, String registrationId) {
        return switch (registrationId) {
            case GOOGLE_REGISTRANT_ID -> new GoogleUser(attributes, registrationId);
            case NAVER_REGISTRANT_ID -> new NaverUser(attributes, registrationId);
            case KAKAO_REGISTRANT_ID -> new KakaoUser(attributes, registrationId);
            default -> throw new IllegalArgumentException("일치하는 제공자가 없습니다.");
        };
    }

    public abstract String getEmail();

    public abstract String getUsername();

    public abstract String getProviderId();

    public OAuth2Provider getProvider() {
        return OAuth2Provider.valueOf(provider.toUpperCase());
    }

    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
}