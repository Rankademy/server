package maruhxn.rankademy.adapter.webapi.dto;

import lombok.Builder;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.UnivInfo;
import maruhxn.rankademy.domain.user.User;

@Builder
public record ProfileResponse(
        Long id,
        String username,
        String email,
        UnivInfoResponse univInfo,
        String description,
        LolPosition mainPosition,
        LolPosition subPosition,
        SummonerInfoResponse summonerInfo // 묶은 객체
) {
    public static ProfileResponse from(User user) {
        UnivInfo univInfo = user.getUnivInfo();
        SummonerInfo summonerInfo = user.getSummonerInfo();
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail().address())
                .univInfo(univInfo == null ?
                        null :
                        new UnivInfoResponse(univInfo.getUnivName(), univInfo.getUnivMail().address(), univInfo.isUnivVerified(), univInfo.getMajor())
                )
                .description(user.getDescription())
                .mainPosition(user.getMainPosition())
                .subPosition(user.getSubPosition())
                .summonerInfo(
                        summonerInfo == null ?
                                null :
                                new SummonerInfoResponse(summonerInfo.getSummonerName(), summonerInfo.getSummonerTag())
                )
                .build();
    }

    public record UnivInfoResponse(String univName, String univMail, boolean univVerified, String major) {
    }

    public record SummonerInfoResponse(
            String summonerName,
            String summonerTag
    ) {
    }
}
