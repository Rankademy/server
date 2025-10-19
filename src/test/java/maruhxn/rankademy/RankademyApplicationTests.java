package maruhxn.rankademy;

import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

class RankademyApplicationTests extends IntegrationTestSupport {

    @Test
    void contextLoads() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            RankademyApplication.main(new String[0]);

            mocked.verify(() -> SpringApplication.run(RankademyApplication.class, new String[0]));
        }
    }

}
