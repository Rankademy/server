package maruhxn.rankademy;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RankademyApplicationTests {

    @Test
    void contextLoads() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            RankademyApplication.main(new String[0]);

            mocked.verify(() -> SpringApplication.run(RankademyApplication.class, new String[0]));
        }
    }

}
