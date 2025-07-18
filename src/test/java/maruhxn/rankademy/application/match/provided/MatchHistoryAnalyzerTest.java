package maruhxn.rankademy.application.match.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.MemberFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class MatchHistoryAnalyzerTest {

    @Autowired
    MatchHistoryAnalyzer matchHistoryAnalyzer;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchDataRepository matchDataRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    MatchHistoryCollector matchHistoryCollector;

    @AfterEach
    void tearDown() {
        matchDataRepository.deleteAll();
    }

    @Test
    void fetchAndAnalyzeMatches() throws IOException {
        Member member = MemberFixture.createMember();
        member.enrollUnivInfo(MemberFixture.createEnrollUnivRequest());
        member.completeUnivAuthentication();
        member.connectSummonerInfo(MemberFixture.createSummonerInfoConnector(), MemberFixture.createRiotAuthRequest());
        memberRepository.save(member);
        em.flush();
        em.clear();

        Long memberId = member.getId();

        String jsonData = getMatchJsonData();

        when(matchHistoryCollector.collectAllMatches(member))
                .thenReturn(Arrays.asList(
                        MatchData.builder()
                                .matchId(UUID.randomUUID().toString())
                                .memberId(memberId)
                                .jsonData(jsonData)
                                .build()));

        matchHistoryAnalyzer.fetchAndAnalyzeMatches(memberId);
        em.flush();
        em.clear();

        member = memberRepository.findById(memberId).get();
        assertThat(matchDataRepository.findAll()).isNotEmpty();
        assertThat(member.getSummonerInfo().getMostChampions()).hasSize(1);
    }

    private static String getMatchJsonData() throws IOException {
        ClassPathResource resource = new ClassPathResource("match-data.json");
        String jsonData;
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            jsonData = FileCopyUtils.copyToString(reader);
        }
        return jsonData;
    }
}
