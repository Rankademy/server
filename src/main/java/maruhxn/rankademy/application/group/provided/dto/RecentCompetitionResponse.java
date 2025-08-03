package maruhxn.rankademy.application.group.provided.dto;

public record RecentCompetitionResponse(
        Long groupId,
        String groupName,
        boolean isWin
) {
}
