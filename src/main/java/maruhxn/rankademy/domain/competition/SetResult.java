package maruhxn.rankademy.domain.competition;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SetResult extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", referencedColumnName = "id")
    Competition competition;

    @Column(nullable = false)
    int setNumber;

    @Column(nullable = false)
    Long winnerTeamId;

    @Column(nullable = false)
    String resultImageKey;

    public SetResult(Competition competition, int setNumber, Long winnerTeamId, String resultImageKey) {
        Assert.isTrue(setNumber > 0, "세트 번호는 0보다 작을 수 없습니다");
        requireNonNull(competition, "대항전 정보는 비어있을 수 없습니다");
        requireNonNull(winnerTeamId, "각 세트의 승리 팀 ID는 비어있을 수 없습니다");
        Assert.isTrue(StringUtils.hasText(resultImageKey), "세트 결과 이미지는 필수입니다");
        this.competition = competition;
        this.setNumber = setNumber;
        this.winnerTeamId = winnerTeamId;
        this.resultImageKey = resultImageKey;
    }

    public static SetResult of(Competition competition, SubmitCompetitionResultRequest.SetResultDto dto) {
        return new SetResult(competition, dto.setNumber(), dto.winnerTeamId(), dto.resultImageKey());
    }
}
