package maruhxn.rankademy;

import maruhxn.rankademy.application.member.required.EmailSender;
import maruhxn.rankademy.domain.member.MemberFixture;
import maruhxn.rankademy.domain.member.PasswordEncoder;
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
}
