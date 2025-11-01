package maruhxn.rankademy.application.team.dto.response;

public record TeamMmrResponse(
        Double teamAvg
) {
    public static TeamMmrResponse empty() {
        return new TeamMmrResponse(0.0);
    }
}
