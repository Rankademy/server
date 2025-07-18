package maruhxn.rankademy.domain.member;

import jakarta.persistence.*;
import lombok.*;
import maruhxn.rankademy.domain.member.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.member.dto.MemberProfileUpdateRequest;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.service.MemberTitleProvider;
import maruhxn.rankademy.domain.member.service.SummonerInfoConnector;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
public class Member extends AbstractEntity {

    @Column(unique = true, length = 20)
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "summoner_info_id")
    private SummonerInfo summonerInfo;

    // TODO: AttributeConverter
    private List<String> titles;

    public List<String> getTitles() {
        return titles == null ? null : java.util.Collections.unmodifiableList(titles);
    }

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

        if ((univInfo == null || !univInfo.univVerified()) ||
                summonerInfo == null) {
            authStatus = MemberAuthStatus.UNAUTHORIZED;
            return false;
        }

        authStatus = MemberAuthStatus.AUTHORIZED;
        return true;
    }


    public void enrollUnivInfo(EnrollUnivRequest enrollUnivRequest) {
        if (this.univInfo == null) {
            this.univInfo = UnivInfo.from(enrollUnivRequest);
            return;
        }

        boolean isEmailChanged = !this.univInfo.univMail().address().equals(enrollUnivRequest.univMail());

        if (isEmailChanged) { // 이메일이 바뀌었으면 학생 재인증 필요
            this.authStatus = MemberAuthStatus.UNAUTHORIZED;
            this.univInfo = UnivInfo.from(enrollUnivRequest);
        } else {
            this.univInfo = this.univInfo.update(enrollUnivRequest);
        }
    }

    public void completeUnivAuthentication() {
        Assert.state(this.univInfo != null, "학교 정보를 등록해주세요.");
        Assert.state(!this.univInfo.univVerified(), "이미 학교 인증이 완료되었습니다.");
        this.univInfo = this.univInfo.authenticate();
    }

    public void removeUnivInfo() {
        this.univInfo = null;
    }

    public void connectSummonerInfo(SummonerInfoConnector summonerInfoConnector, RiotAuthRequest riotAuthRequest) {
        Assert.state(this.summonerInfo == null, "이미 라이엇 계정이 연동되었습니다.");
        this.summonerInfo = summonerInfoConnector.connect(riotAuthRequest);
    }

    public void removeRiotAuthentication() {
        this.summonerInfo = null;
    }

    public void updateProfile(MemberProfileUpdateRequest memberProfileUpdateRequest) {
        this.username = memberProfileUpdateRequest.username();
        this.description = memberProfileUpdateRequest.description();
        this.mainPosition = memberProfileUpdateRequest.mainPosition();
        this.subPosition = memberProfileUpdateRequest.subPosition();
    }

    public void updateTitles(MemberTitleProvider titleProvider) {
        Assert.state(this.getId() != null, "ID가 null일 수 없습니다.");

        this.titles = titleProvider.getTitles(this.getId());
    }
}
