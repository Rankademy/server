package maruhxn.rankademy.adapter.webapi.dto;

import lombok.Builder;
import maruhxn.rankademy.domain.member.LolPosition;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.SummonerInfo;
import maruhxn.rankademy.domain.member.UnivInfo;

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
    public static ProfileResponse from(Member member) {
        UnivInfo univInfo = member.getUnivInfo();
        SummonerInfo summonerInfo = member.getSummonerInfo();
        return ProfileResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail().address())
                .univInfo(univInfo == null ?
                        null :
                        new UnivInfoResponse(univInfo.univName(), univInfo.univMail().address(), univInfo.univVerified(), univInfo.major())
                )
                .description(member.getDescription())
                .mainPosition(member.getMainPosition())
                .subPosition(member.getSubPosition())
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
