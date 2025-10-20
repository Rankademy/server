package maruhxn.rankademy;

import maruhxn.rankademy.application.match.RiotAuthEventHandler;
import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.application.user.required.UnivMailCertifier;
import maruhxn.rankademy.domain.user.UserFixture;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RankademyTestConfiguration {

    @Bean
    public EmailSender emailSender() {
        return (email, subject, body) -> System.out.println("Sending email: " + email);
    }

    @Bean
    public UnivMailCertifier univMailCertifier() {
        return new UnivMailCertifier() {
            @Override
            public void sendCertifyMail(String email, String univName, int code) {
                System.out.println("Sending email: " + email + " code: " + code);
            }

            @Override
            public void certifyCode(String email, String univName, int code) {
                System.out.println("Certifying code: " + code);
            }
        };
    }

    @Bean
    public SummonerInfoConnector summonerInfoConnector() {
        return UserFixture::createSummonerInfo;
    }

    @Bean
    public RiotAuthEventHandler riotAuthEventHandler() {
        return new RiotAuthEventHandler(userId -> System.out.println("Refreshing matches for user: " + userId));
    }

}
