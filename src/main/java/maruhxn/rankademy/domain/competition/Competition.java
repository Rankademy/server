package maruhxn.rankademy.domain.competition;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Table(name = "competitions")
@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Competition extends AbstractEntity {

    @ManyToOne
    private Team team1;

    @ManyToOne
    private Team team2;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompetitionStatus status;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private int totalSets;

    @Column(nullable = false)
    private Long finalWinnerTeamId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "set_results",
            joinColumns = @JoinColumn(name = "competition_id"))
    private List<SetResult> setResults = new ArrayList<>();

    public Competition(Team team1, Team team2) {
        this.team1 = requireNonNull(team1);
        this.team2 = requireNonNull(team2);
        this.status = CompetitionStatus.SCHEDULED;
        this.scheduledAt = LocalDateTime.now();
    }

    public static Competition createAfterAccept(Team team1, Team team2) {
        return new Competition(team1, team2);
    }

    public void submitSetResult(SubmitCompetitionResultRequest request) {
        Assert.isTrue(validateTeams(request), "세트의 팀 구성이 대항전과 다릅니다.");
        Assert.state(this.status == CompetitionStatus.SCHEDULED, "이미 진행된 대항전입니다.");
        this.totalSets = request.totalSets();
        this.finalWinnerTeamId = request.finalWinnerId();
        this.memo = request.memo();
        this.status = CompetitionStatus.RESULT_SUBMITTED;
        this.setResults = request.setResults().stream().map(SetResult::of).toList();
    }

    private boolean validateTeams(SubmitCompetitionResultRequest request) {
        return Set.of(request.team1Id(), request.team2Id())
                .containsAll(Set.of(team1.getId(), team2.getId()));
    }

    public Team getFinalWinner() {
        return finalWinnerTeamId.equals(team1.getId()) ? team1 : team2;
    }

}
