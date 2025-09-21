package maruhxn.rankademy.domain.competition;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.springframework.util.Assert;

import java.time.Duration;
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

    private Long team1Id;

    private Long team2Id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompetitionStatus status;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private int totalSets;

    private Long finalWinnerTeamId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime submittedAt;

    private LocalDateTime expiredAt;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SetResult> setResults = new ArrayList<>();

    private String opposedReason;

    public Competition(Long team1Id, Long team2Id) {
        this.team1Id = requireNonNull(team1Id);
        this.team2Id = requireNonNull(team2Id);
        this.status = CompetitionStatus.SCHEDULED;
        this.scheduledAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusDays(7);
    }

    public static Competition createAfterAccept(Long team1Id, Long team2Id) {
        return new Competition(team1Id, team2Id);
    }

    public void submitSetResult(SubmitCompetitionResultRequest request, LocalDateTime submittedAt) {
        Assert.state(submittedAt.isBefore(this.expiredAt), "일주일이 지난 대항전에 대해서는 경기 결과를 등록할 수 없습니다.");
        Assert.isTrue(validateTeams(request), "세트의 팀 구성이 대항전과 다릅니다.");
        Assert.state(this.status == CompetitionStatus.SCHEDULED, "이미 진행된 대항전입니다.");
        this.totalSets = request.totalSets();
        this.finalWinnerTeamId = request.finalWinnerId();
        this.memo = request.memo();
        this.status = CompetitionStatus.COMPLETED;
        this.submittedAt = submittedAt;

        request.setResults().stream()
                .map(dto -> SetResult.of(this, dto))
                .forEach(setResult -> this.setResults.add(setResult));
    }

    private boolean validateTeams(SubmitCompetitionResultRequest request) {
        return Set.of(request.team1Id(), request.team2Id())
                .containsAll(Set.of(requireNonNull(team1Id), requireNonNull(team2Id)));
    }

    public Long getFinalWinnerId() {
        return finalWinnerTeamId.equals(team1Id) ? team1Id : team2Id;
    }

    public void oppose(OpposeResultRequest request, LocalDateTime now) {
        Assert.state(this.status == CompetitionStatus.COMPLETED, "등록 완료된 경기 결과에 대해서만 이의 신청이 가능합니다.");
        Duration diff = Duration.between(this.submittedAt, now).abs();
        Assert.state(diff.compareTo(Duration.ofDays(7)) < 0, "제출일로부터 7일이 지난 건에 대해서는 이의 신청이 불가합니다.");
        this.status = CompetitionStatus.OPPOSED;
        this.opposedReason = request.reason();
    }

}
