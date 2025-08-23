package maruhxn.rankademy.domain.competition;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import org.springframework.util.StringUtils;

@Embeddable
public record SetResult(
        @Column(nullable = false)
        int setNumber,

        @Column(nullable = false)
        Long winnerTeamId,

        @Column(nullable = false)
        String resultImageKey
) {
    public SetResult {
        if (setNumber <= 0) throw new IllegalArgumentException("세트 번호는 0보다 작을 수 없습니다");
        if (!StringUtils.hasText(resultImageKey)) {
            throw new IllegalArgumentException("세트 결과 이미지는 필수입니다.");
        }
    }

    public static SetResult of(SubmitCompetitionResultRequest.SetResultDto dto) {
        return new SetResult(dto.setNumber(), dto.winnerTeamId(), dto.resultImageKey());
    }
}
