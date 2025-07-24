package maruhxn.rankademy.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Tier {
    UNRANKED(-1),
    IRON(0),
    BRONZE(500),
    SILVER(1000),
    GOLD(1500),
    PLATINUM(2000),
    EMERALD(2500),
    DIAMOND(3000),
    MASTER(3500), // 마스터부터 별도 LP 처리
    GRANDMASTER(3500),
    CHALLENGER(3500);

    private final int score;
}
