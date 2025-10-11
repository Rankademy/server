package maruhxn.rankademy.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Rank {
    I(400), II(300), III(200), IV(100), EMPTY(0);

    private final int score;
}
