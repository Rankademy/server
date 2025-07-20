package maruhxn.rankademy.adapter.security;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.filter.JwtExceptionFilter;
import maruhxn.rankademy.adapter.security.filter.JwtVerificationFilter;
import maruhxn.rankademy.adapter.security.filter.RestLoginFilter;
import maruhxn.rankademy.adapter.security.handlers.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider restAuthenticationProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtVerificationFilter jwtVerificationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final RestAuthenticationSuccessHandler authenticationSuccessHandler;
    private final RestAuthenticationFailureHandler authenticationFailureHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final RestLogoutSuccessHandler logoutSuccessHandler;
//    private final RankademyOAuth2UserService  oAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(restAuthenticationProvider);
        AuthenticationManager authenticationManager = builder.build();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz ->
                        {
                            Arrays.stream(PermitAllUrls.values()).forEach(url -> {
                                authz.requestMatchers(url.getMethod(), url.getUrl()).permitAll();
                            });

                            authz.anyRequest().authenticated();
                        }
                )
                .logout(
                        logout -> logout
                                .clearAuthentication(true)
                                .invalidateHttpSession(true)
                                .logoutUrl("/api/v1/auth/logout")
                                .logoutSuccessHandler(logoutSuccessHandler)
                )
                .addFilterBefore(restLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtVerificationFilter, RestLoginFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtVerificationFilter.class)
                .authenticationManager(authenticationManager)
                .exceptionHandling(eh ->
                        eh
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    private RestLoginFilter restLoginFilter(AuthenticationManager authenticationManager) {
        RestLoginFilter restLoginFilter = new RestLoginFilter();
        restLoginFilter.setAuthenticationManager(authenticationManager);
        restLoginFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        restLoginFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        return restLoginFilter;
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
