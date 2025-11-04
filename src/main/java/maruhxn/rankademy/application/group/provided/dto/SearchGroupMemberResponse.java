package maruhxn.rankademy.application.group.provided.dto;

public record SearchGroupMemberResponse(
        Long userId,
        String summonerName,
        String summonerTag,
        int summonerIcon
) {
}
