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
import maruhxn.rankademy.domain.user.service.UserLabelProvider;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "labels",
            joinColumns = @JoinColumn(name = "user_id"))
    private List<Label> labels = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "refresh_tokens",
            joinColumns = @JoinColumn(name = "user_id"))
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupMember> groupMembers = new HashSet<>();

    private LocalDateTime lastLoginAt;

    private LocalDateTime lastLabelUpdatedAt;

    @Embedded
    private EffectiveStrength effectiveStrength;

    public User(String username, Email email) {
        this.username = username;
        this.email = email;
        this.joinedAt = LocalDateTime.now();
        this.authStatus = UserAuthStatus.UNAUTHORIZED;
        this.role = Role.ROLE_USER;
        this.effectiveStrength = new EffectiveStrength();
        this.univInfo = UnivInfo.empty();
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
        if (!univInfo.isAuthorized() || summonerInfo == null) {
            authStatus = UserAuthStatus.UNAUTHORIZED;
            return;
        }
        authStatus = UserAuthStatus.AUTHORIZED;
    }

    public void completeUnivAuthentication(EnrollUnivRequest enrollUnivRequest) {
        Assert.state(!this.univInfo.isAuthorized(), "이미 학교 인증이 완료되었습니다.");
        this.univInfo = UnivInfo.from(enrollUnivRequest);
        checkAuthorized();
    }

    public void removeUnivInfo() {
        this.univInfo = new UnivInfo(
                null,
                null,
                this.univInfo.getAdmissionYear(),
                this.univInfo.getMajor()
        );
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
        if(StringUtils.hasText(profileUpdateRequest.username())) this.username = profileUpdateRequest.username();
        if(StringUtils.hasText(profileUpdateRequest.description())) this.description = profileUpdateRequest.description();
        if(profileUpdateRequest.mainPosition() != null) this.mainPosition = profileUpdateRequest.mainPosition();
        if(profileUpdateRequest.subPosition() != null) this.subPosition = profileUpdateRequest.subPosition();
        if(StringUtils.hasText(profileUpdateRequest.major())) this.univInfo.updateMajor(profileUpdateRequest.major());
        if(profileUpdateRequest.admissionYear() != null)  this.univInfo.updateAdmissionYear(profileUpdateRequest.admissionYear());
        Assert.state(
                (this.mainPosition == LolPosition.ANY && this.subPosition == LolPosition.ANY)
                        || this.mainPosition != this.subPosition,
                "주 포지션과 부 포지션은 달라야 합니다."
        );
    }

    public void updateLabels(UserLabelProvider labelProvider) {
        this.labels.clear();
        this.labels = labelProvider.getLabels(this.getSummonerInfo().getPuuid()).stream()
                .map(Label::new).toList();
        this.lastLabelUpdatedAt = LocalDateTime.now();
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
