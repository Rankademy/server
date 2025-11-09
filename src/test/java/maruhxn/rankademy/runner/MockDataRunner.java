package maruhxn.rankademy.runner;

import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;

@Disabled
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
//        "spring.datasource.url=jdbc:mysql://52.78.217.240:3306/rankademy?rewriteBatchedStatements=true",
        "spring.datasource.username=maruhxn",
        "spring.datasource.password=z73fYd7MvrRBqM60Pnsh",
        "spring.jpa.hibernate.ddl-auto=create"
})
public class MockDataRunner extends IntegrationTestSupport {

    @Autowired
    MockUserDataInsertRunner userDataInsertRunner;

    @Autowired
    MockGroupDataInsertRunner groupDataInsertRunner;

    @Autowired
    MockTeamDataInsertRunner teamDataInsertRunner;

    @Autowired
    MockScrimTeamDataInsertRunner scrimTeamDataInsertRunner;

    @Autowired
    MockCompetitionDataInsertRunner competitionDataInsertRunner;

    @Test
    @Commit
    void insertAllData() throws Exception {
        userDataInsertRunner.run();
        groupDataInsertRunner.run();
        teamDataInsertRunner.run();
        scrimTeamDataInsertRunner.run();
        competitionDataInsertRunner.run();
    }

}
