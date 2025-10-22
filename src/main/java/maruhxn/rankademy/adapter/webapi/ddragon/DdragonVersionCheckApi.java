package maruhxn.rankademy.adapter.webapi.ddragon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.integration.riot.DdragonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "Ddragon Version Checks", description = "Ddragon 버전 체크 API")
@RestController
@RequestMapping("/api/v1/ddragon")
@RequiredArgsConstructor
public class DdragonVersionCheckApi {

    private final DdragonClient ddragonClient;

    /**
     * https://ddragon.leagueoflegends.com/api/versions.json
     */
    @Operation(summary = "ddragon API의 최신 버전을 조회합니다.")
    @GetMapping("/latest-version")
    @Cacheable(value = "ddragonLatestVersion", key = "'latest'")
    public String getLatestVersion() throws IOException {
        return ddragonClient.getLatestVersion();
    }
}
