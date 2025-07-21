package maruhxn.rankademy.adapter.persistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = "maruhxn.rankademy.domain")
@EnableJpaRepositories(basePackages = "maruhxn.rankademy.application")
public class JpaConfig {
}
