package maruhxn.rankademy.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.group.GroupMember;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import maruhxn.rankademy.domain.user.service.UserTitleProvider;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Table(name = "users")
@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
public class User extends AbstractEntity {

    @Column(unique = true, length = 100)
    private String username;

    @NaturalId
    private Email email;

    @Enumerated(EnumType.STRING)
    private UserAuthStatus authStatus;

    private String description;

    private LocalDateTime joinedAt;

    @Column(name = "main_position", nullable = false)
    @Enumerated(EnumType.STRING)
    private LolPosition mainPosition = LolPosition.ANY;

    @Column(name = "sub_position", nullable = false)
    @Enumerated(EnumType.STRING)
    private LolPosition subPosition = LolPosition.ANY;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "oauth_account_id")
    private Set<OAuthAccount> oauthAccounts = new HashSet<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "univMail.address", column = @Column(name = "univ_mail", length = 150))
    })
    private UnivInfo univInfo;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "summoner_info_id")
    private SummonerInfo summonerInfo;

    // TODO: AttributeConverter
    private List<String> titles;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "refresh_tokens",
            joinColumns = @JoinColumn(name = "user_id"))
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupMember> groupMembers = new HashSet<>();

    private LocalDateTime lastLoginAt;

    @Embedded
    private EffectiveStrength effectiveStrength;

    public User(String username, Email email) {
        this.username = username;
        this.email = email;
        this.joinedAt = LocalDateTime.now();
        this.authStatus = UserAuthStatus.UNAUTHORIZED;
        this.role = Role.ROLE_USER;
        this.effectiveStrength = new EffectiveStrength();
    }

    public static User oauth2Register(UserOAuth2CreateRequest userOAuth2CreateRequest) {
        User user = new User(
                requireNonNull(userOAuth2CreateRequest.username()),
                new Email(userOAuth2CreateRequest.email())
        );

        user.addOAuthAccount(userOAuth2CreateRequest.provider(), userOAuth2CreateRequest.providerId());

        return user;
    }

    public boolean isAuthorized() {
        checkAuthorized();
        return authStatus == UserAuthStatus.AUTHORIZED;
    }

    private void checkAuthorized() {
        if ((univInfo == null || !univInfo.isUnivVerified()) ||
                summonerInfo == null) {
            authStatus = UserAuthStatus.UNAUTHORIZED;
            return;
        }
        authStatus = UserAuthStatus.AUTHORIZED;
    }

    public void enrollUnivInfo(EnrollUnivRequest enrollUnivRequest) {
        if (this.univInfo == null) {
            this.univInfo = UnivInfo.from(enrollUnivRequest);
            return;
        }

        boolean isEmailChanged = !this.univInfo.getUnivMail().address().equals(enrollUnivRequest.univMail());

        if (isEmailChanged) { // 이메일이 바뀌었으면 학생 재인증 필요
            this.authStatus = UserAuthStatus.UNAUTHORIZED;
            this.univInfo = UnivInfo.from(enrollUnivRequest);
        } else {
            this.univInfo = this.univInfo.update(enrollUnivRequest);
        }
    }

    public void completeUnivAuthentication() {
        Assert.state(this.univInfo != null, "학교 정보를 등록해주세요.");
        Assert.state(!this.univInfo.isUnivVerified(), "이미 학교 인증이 완료되었습니다.");
        this.univInfo = this.univInfo.authenticate();
        checkAuthorized();
    }

    public void removeUnivInfo() {
        this.univInfo = null;
    }

    public void connectSummonerInfo(SummonerInfoConnector summonerInfoConnector, RiotAuthRequest riotAuthRequest) {
        Assert.state(this.summonerInfo == null, "이미 라이엇 계정이 연동되었습니다.");
        this.summonerInfo = summonerInfoConnector.connect(riotAuthRequest);
        checkAuthorized();
    }

    public void removeRiotAuthentication() {
        this.summonerInfo = null;
    }

    public void updateProfile(ProfileUpdateRequest profileUpdateRequest) {
        Assert.state(
                profileUpdateRequest.mainPosition() != profileUpdateRequest.subPosition(),
                "주 포지션과 부 포지션은 달라야 합니다."
        );
        this.username = profileUpdateRequest.username();
        this.description = profileUpdateRequest.description();
        this.mainPosition = profileUpdateRequest.mainPosition();
        this.subPosition = profileUpdateRequest.subPosition();
    }

    public void updateTitles(UserTitleProvider titleProvider) {
        Assert.state(this.getId() != null, "ID가 null일 수 없습니다.");

        this.titles = titleProvider.getTitles(this.getId());
    }

    public void invalidateAllTokens() {
        this.refreshTokens.clear();
    }

    public void addRefreshToken(String refreshToken) {
        this.refreshTokens.add(new RefreshToken(refreshToken));
    }

    public void rotateRefreshToken(String oldToken, String newToken) {
        this.refreshTokens.remove(new RefreshToken(oldToken));
        this.refreshTokens.add(new RefreshToken(newToken));
    }

    public void addOAuthAccount(OAuth2Provider provider, String oauthId) {
        this.oauthAccounts.add(new OAuthAccount(provider, oauthId));
    }

    public String getFullSummonerName() {
        return "%s#%s".formatted(this.summonerInfo.getSummonerName(), this.summonerInfo.getSummonerTag());
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateEffectiveStrength(EffectiveStrength effectiveStrength) {
        this.effectiveStrength = effectiveStrength;
    }
}
