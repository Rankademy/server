package maruhxn.rankademy.domain.match;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "match_data")
@Getter
public class MatchData {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("match_id")
    private String matchId; // Riot에서 제공하는 고유 매치 ID

    @Indexed
    @Field("user_id")
    private Long userId; // User 엔티티 참조용

    @Field("json_data")
    String jsonData;

    @Builder
    public MatchData(String matchId, Long userId, String jsonData) {
        this.matchId = matchId;
        this.userId = userId;
        this.jsonData = jsonData;
    }
}
