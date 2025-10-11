package maruhxn.rankademy.adapter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rankademyOpenApi() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT 기반 인증 토큰");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Rankademy API")
                        .version("v1")
                        .description("Rankademy 서비스 API 명세")
                        .contact(new Contact()
                                .name("Rankademy")
                                .email("support@rankademy.app"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(
                        new Server().url("/").description("Default Server")
                ));
    }

    @Bean
    public GroupedOpenApi rankademyApi() {
        return GroupedOpenApi.builder()
                .group("rankademy-v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
