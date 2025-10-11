package maruhxn.rankademy.domain.competitionrequest;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.shared.AbstractEntity;
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
    private Long fromTeamId; // 요청 보낸 팀

    @Column(nullable = false)
    private Long toTeamId; // 요청 받는 팀

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionRequestStatus status = CompetitionRequestStatus.PENDING;

    public CompetitionRequest(Long fromTeamId, Long toTeamId, LocalDateTime now) {
        this.fromTeamId = requireNonNull(fromTeamId);
        this.toTeamId = requireNonNull(toTeamId);
        this.requestedAt = now;
    }

    public void accept() {
        Assert.isTrue(status == CompetitionRequestStatus.PENDING, "대기 중인 요청이 아닙니다.");
        this.status = CompetitionRequestStatus.ACCEPTED;
    }

    public void reject() {
        Assert.isTrue(status == CompetitionRequestStatus.PENDING, "대기 중인 요청이 아닙니다.");
        this.status = CompetitionRequestStatus.REJECTED;
    }
}
