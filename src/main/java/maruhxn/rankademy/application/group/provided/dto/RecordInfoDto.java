package maruhxn.rankademy.application.group.provided.dto;

public record RecordInfoDto(
        int winCount,
        int lossCount,
        double winRate
) {
    public RecordInfoDto(int winCount, int lossCount) {
        this(winCount, lossCount, (double) winCount / (winCount + lossCount));
    }
}
