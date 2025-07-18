package maruhxn.rankademy.adapter.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.domain.match.service.MyChampionIdParser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyChampionIdParserImpl implements MyChampionIdParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String parse(String json, String puuid) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode info = root.path("info");

            JsonNode participants = info.path("participants");
            JsonNode myParticipant = null;
            for (JsonNode participant : participants) {
                if (participant.path("puuid").asText().equals(puuid)) {
                    myParticipant = participant;
                    break;
                }
            }

            if (myParticipant == null) {
                throw new IllegalArgumentException("매치 정보에 PUUID가 일치하는 소환사 정보가 존재하지 않습니다.");
            }

            return myParticipant.path("championName").asText(); // 또는 championId
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
