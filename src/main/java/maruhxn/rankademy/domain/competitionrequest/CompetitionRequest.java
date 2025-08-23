package maruhxn.rankademy.domain.competitionrequest;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Table(
        name = "competition_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "competition_request_unique",
                        columnNames = {"from_team_id", "to_team_id"}
                )
        }
)
@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompetitionRequest extends AbstractEntity {

    @Column(nullable = false)
    private Long fromTeam; // 요청 보낸 팀

    @Column(nullable = false)
    private Long toTeam; // 요청 받는 팀

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionRequestStatus status = CompetitionRequestStatus.PENDING;

    public CompetitionRequest(Long fromTeam, Long toTeam) {
        this.fromTeam = requireNonNull(fromTeam);
        this.toTeam = requireNonNull(toTeam);
        this.requestedAt = LocalDateTime.now();
    }

    public void accept(Team from, Team to) {
        Assert.isTrue(!from.getGroupId().equals(to.getGroupId()), "같은 그룹끼리는 대항전 요청을 보낼 수 없습니다.");
        Assert.isTrue(status == CompetitionRequestStatus.PENDING, "대기 중인 요청이 아닙니다.");
        this.status = CompetitionRequestStatus.ACCEPTED;
    }

    public void reject() {
        Assert.isTrue(status == CompetitionRequestStatus.PENDING, "대기 중인 요청이 아닙니다.");
        this.status = CompetitionRequestStatus.REJECTED;
    }
}
