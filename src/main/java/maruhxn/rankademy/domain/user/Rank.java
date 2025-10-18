package maruhxn.rankademy.domain.user;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Rank {
    I(400), II(300), III(200), IV(100), EMPTY(0);

    private final int score;

    @JsonValue
    public int jsonValue() {
        return switch (this) {
            case I -> 1;
            case II -> 2;
            case III -> 3;
            case IV -> 4;
            case EMPTY -> 0;
        };
    }
}
