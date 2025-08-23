package maruhxn.rankademy.domain.team;

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
public class TeamMember extends AbstractEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "team_member_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LolPosition position;

    public TeamMember(User user, LolPosition position) {
        this.user = user;
        this.position = position;
    }
}
