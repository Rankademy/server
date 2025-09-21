package maruhxn.rankademy.domain.scrim_team;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;

@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrimTeamMember extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrim_team_id")
    private ScrimTeam scrimTeam;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LolPosition position;

    public ScrimTeamMember(User user, LolPosition position) {
        this.user = user;
        this.position = position;
    }

    // === 연관관계 메서드 ===
    public void setScrimTeam(ScrimTeam scrimTeam) {
        this.scrimTeam = scrimTeam;
    }
}
