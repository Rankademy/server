package maruhxn.rankademy.domain.member;

import jakarta.persistence.*;
import lombok.*;
import maruhxn.rankademy.domain.member.dto.CertifySummonerInfoRequest;
import maruhxn.rankademy.domain.member.dto.CertifyUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
public class Member extends AbstractEntity {

    @Column(unique = true)
    private String username;

    @NaturalId
    private Email email;

    private String passwordHash;

    private MemberAuthStatus authStatus;

    private String description;

    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    private LolPosition mainPosition;

    @Enumerated(EnumType.STRING)
    private LolPosition subPosition;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "univMail.address", column = @Column(name = "univ_mail", length = 150))
    })
    private UnivInfo univInfo;

    @OneToOne
    @JoinColumn(name = "summoner_info_id")
    private SummonerInfo summonerInfo;

    @Builder
    public Member(String username, Email email, String passwordHash, MemberAuthStatus authStatus, String description, LocalDateTime joinedAt, LolPosition mainPosition, LolPosition subPosition, Role role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.authStatus = authStatus;
        this.description = description;
        this.joinedAt = joinedAt;
        this.mainPosition = mainPosition;
        this.subPosition = subPosition;
        this.role = role;
    }

    public static Member register(MemberRegisterRequest registerRequest, PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(new Email(registerRequest.email()))
                .username(requireNonNull(registerRequest.username()))
                .passwordHash(requireNonNull(passwordEncoder.encode(registerRequest.password())))
                .authStatus(MemberAuthStatus.UNAUTHORIZED)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public boolean verifyPassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.passwordHash);
    }

    public void changePassword(String password, PasswordEncoder passwordEncoder) {
        this.passwordHash = passwordEncoder.encode(requireNonNull(password));
    }

    public boolean isAuthorized() {
        Assert.state(authStatus == MemberAuthStatus.UNAUTHORIZED, "이미 인증처리가 완료되었습니다.");
        Assert.state(univInfo != null, "학교 인증을 완료해주세요");
        Assert.state(summonerInfo != null, "소환사 정보를 등록해주세요");

        if (univInfo != null && summonerInfo != null) {
            authStatus = MemberAuthStatus.AUTHORIZED;
        }

        return authStatus == MemberAuthStatus.AUTHORIZED;
    }

    public void completeUniversityAuthentication(CertifyUnivRequest certifyUnivRequest) {
        Assert.state(authStatus == MemberAuthStatus.UNAUTHORIZED, "이미 인증처리가 완료되었습니다.");
        this.univInfo = UnivInfo.from(certifyUnivRequest);
    }

    public void removeUniversityAuthentication() {
        this.univInfo = null;
        this.authStatus = MemberAuthStatus.UNAUTHORIZED;
    }

    public void completeRiotAuthentication(CertifySummonerInfoRequest certifySummonerInfoRequest) {
        Assert.state(authStatus == MemberAuthStatus.UNAUTHORIZED, "이미 인증처리가 완료되었습니다.");
        this.summonerInfo = SummonerInfo.from(certifySummonerInfoRequest);
    }

    public void removeRiotAuthentication() {
        this.summonerInfo = null;
        this.authStatus = MemberAuthStatus.UNAUTHORIZED;
    }
}
