package maruhxn.rankademy.support;

import maruhxn.rankademy.RankademyTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(properties = "de.flapdoodle.mongodb.embedded.version=8.0.12")
@Import(RankademyTestConfiguration.class)
public class IntegrationTestSupport {

}
