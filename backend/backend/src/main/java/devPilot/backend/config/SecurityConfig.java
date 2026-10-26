package devPilot.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import devPilot.backend.security.GithubOAuth2UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final GithubOAuth2UserService gitHubOAuth2UserService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${SESSION_COOKIE_SAME_SITE:lax}")
    private String sessionCookieSameSite;

    @Value("${SESSION_COOKIE_SECURE:false}")
    private String sessionCookieSecure;

    @PostConstruct
    void logStartupConfig() {
        log.info("DevPilot auth config: frontendUrl={}, sessionCookieSameSite={}, sessionCookieSecure={}",
                frontendUrl, sessionCookieSameSite, sessionCookieSecure);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler oauth2SuccessHandler,
            AuthenticationFailureHandler oauth2FailureHandler) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login-url",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(gitHubOAuth2UserService))
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("DEVPILOT_SESSION"));

        return http.build();
    }

    @Bean
    AuthenticationSuccessHandler oauth2SuccessHandler(
            @Value("${app.frontend-url}") String configuredFrontendUrl) {
        String normalizedFrontendUrl = configuredFrontendUrl.replaceAll("/+$", "");
        String redirectTarget = normalizedFrontendUrl + "/auth/callback";
        SimpleUrlAuthenticationSuccessHandler delegate = new SimpleUrlAuthenticationSuccessHandler();
        delegate.setDefaultTargetUrl(redirectTarget);
        return (request, response, authentication) -> {
            log.info("OAuth2 login success: sessionPresent={}, redirectTarget={}",
                    request.getSession(false) != null, redirectTarget);
            delegate.onAuthenticationSuccess(request, response, authentication);
        };
    }

    @Bean
    AuthenticationFailureHandler oauth2FailureHandler(
            @Value("${app.frontend-url}") String configuredFrontendUrl) {
        String normalizedFrontendUrl = configuredFrontendUrl.replaceAll("/+$", "");
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl(normalizedFrontendUrl + "/login?error=oauth_failed");
        return (request, response, exception) -> {
            log.warn("OAuth2 login failure: {}", exception.getClass().getSimpleName());
            handler.onAuthenticationFailure(request, response, exception);
        };
    }
}