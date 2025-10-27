package maruhxn.rankademy.adapter.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import maruhxn.rankademy.adapter.security.dto.TokenDto;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import static maruhxn.rankademy.adapter.security.Constants.BEARER_PREFIX;

@Component
public class JwtProvider {

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    private SecretKey secretKey;
    private JwtParser jwtParser;

    public JwtProvider(@Value("${jwt.secret-key}") String secretKeyStr) {
        this.secretKey = new SecretKeySpec(secretKeyStr.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.jwtParser = Jwts.parser()
                .verifyWith(this.secretKey)
                .build();
    }

    public RankademyUser getPrincipal(String accessToken) {
        UserInfo userInfo = this.extractUserInfoFromAccessToken(accessToken);
        return RankademyUser.from(userInfo);
    }

    public UserInfo extractUserInfoFromAccessToken(String accessToken) {
        return UserInfo.builder()
                .id(this.getId(accessToken))
                .username(this.getUsername(accessToken))
                .email(this.getEmail(accessToken))
                .isAuthorized(this.getIsAuthorized(accessToken))
                .role(this.getRole(accessToken))
//                .provider(this.getProvider(accessToken))
                .build();
    }

    /**
     * Bearer Prefix를 포함한 값을 전달받으면 토큰만을 추출하여 반환
     *
     * @param bearerToken
     * @return Token (String)
     */
    public String getTokenFromBearer(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.split(" ")[1];
        }
        return null;
    }

    public TokenDto createJwt(RankademyUser rankademyUser) {
        String accessToken = this.generateAccessToken(rankademyUser, new Date());
        String refreshToken = this.generateRefreshToken(rankademyUser.getEmail(), new Date());
        Integer summonerIcon = rankademyUser.userInfo().summonerIcon();
        return new TokenDto(
                rankademyUser.getEmail(),
                accessToken,
                refreshToken,
                summonerIcon
        );
    }

    public String generateAccessToken(RankademyUser rankademyUser, Date now) {
        ArrayList<? extends GrantedAuthority> authorities =
                (ArrayList<? extends GrantedAuthority>) rankademyUser.getAuthorities();

        return Jwts.builder()
                .subject(rankademyUser.getEmail())
                .claim("id", rankademyUser.getId())
                .claim("summonerName", rankademyUser.getNickname())
                .claim("isAuthorized", rankademyUser.userInfo().isAuthorized())
                .claim("role", authorities.get(0).getAuthority())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String email, Date now) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public void validate(String token) {
        try {
            this.getPayload(token);
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    /**
     * 서명된 토큰 값을 파싱하여 payload를 추출
     *
     * @param token
     * @return claims(payload)
     */
    public Claims getPayload(String token) {
        return jwtParser
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getId(String token) {
        return getPayload(token).get("id", Long.class);
    }

    public String getEmail(String token) {
        return getPayload(token).getSubject();
    }

    public String getUsername(String token) {
        return getPayload(token).get("summonerName", String.class);
    }

//    public String getProvider(String token) {
//        return getPayload(token).get("provider", String.class);
//    }

    public String getRole(String token) {
        return getPayload(token).get("role", String.class);
    }

    private boolean getIsAuthorized(String token) {
        return getPayload(token).get("isAuthorized", Boolean.class);
    }

//    public Map<String, String> buildCookieFromTokenDto(TokenDto tokenDto) {
//        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenDto.accessToken())
//                .httpOnly(true)
//                .sameSite(Cookie.SameSite.LAX.name())
//                .path("/")
//                .maxAge(accessTokenExpiration / 1000)
//                .build();
//
//        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.refreshToken())
//                .httpOnly(true)
//                .sameSite(Cookie.SameSite.LAX.name())
//                .path("/")
//                .maxAge(refreshTokenExpiration / 1000)
//                .build();
//
//        return Map.of(
//                "accessToken", accessTokenCookie.toString(),
//                "refreshToken", refreshTokenCookie.toString()
//        );
//    }
}
