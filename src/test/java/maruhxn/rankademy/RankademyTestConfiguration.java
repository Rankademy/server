package maruhxn.rankademy;

import maruhxn.rankademy.application.member.required.EmailSender;
import maruhxn.rankademy.application.member.required.UnivMailCertifier;
import maruhxn.rankademy.domain.member.MemberFixture;
import maruhxn.rankademy.domain.member.PasswordEncoder;
import maruhxn.rankademy.domain.member.service.SummonerInfoConnector;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RankademyTestConfiguration {

    @Bean
    public EmailSender emailSender() {
        return (email, subject, body) -> System.out.println("Sending email: " + email);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return MemberFixture.createPasswordEncoder();
    }

    @Bean
    public UnivMailCertifier univMailCertifier() {
        return new UnivMailCertifier() {
            @Override
            public void sendCertifyMail(String email, String univName, boolean inCollege) {
                System.out.println("Sending email: " + email);
            }

            @Override
            public void certifyCode(String email, String univName, int code) {
                System.out.println("Certifying code: " + code);
            }
        };
    }

    @Bean
    public SummonerInfoConnector summonerInfoConnector() {
        return MemberFixture::createSummonerInfo;
    }

}
