package ru.mirea.project.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import ru.mirea.project.account.models.UserAccount;
import ru.mirea.project.account.repositories.UserAccountRepository;

import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserAccountRepository repository
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/change-password"
                        ).authenticated()

                        .requestMatchers(
                                "/api/admin/**",
                                "/api/audit/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/**",
                                "/api/cars/**",
                                "/api/requests/**"
                        ).hasAnyRole("VIEWER", "USER", "ADMIN")

                        .requestMatchers(
                                "/api/users/**",
                                "/api/cars/**",
                                "/api/requests/**"
                        ).hasAnyRole("USER", "ADMIN")

                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(
                                token -> convertJwt(token, repository)
                        )
                ));

        return http.build();
    }

    private JwtAuthenticationToken convertJwt(
            Jwt jwt,
            UserAccountRepository repository
    ) {

        UUID id;

        try {
            id = UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw invalidToken();
        }

        UserAccount account = repository.findById(id)
                .filter(UserAccount::isActive)
                .orElseThrow(this::invalidToken);

        String authority;

        if (account.isMustChangePassword() ||
                Boolean.TRUE.equals(
                        jwt.getClaimAsBoolean("temporary")
                )) {

            authority = "PASSWORD_CHANGE_REQUIRED";

        } else {

            authority = "ROLE_" + account.getRole().name();
        }

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority(authority)),
                account.getId().toString()
        );
    }

    private OAuth2AuthenticationException invalidToken() {

        return new OAuth2AuthenticationException(
                new OAuth2Error(
                        "invalid_token",
                        "Учётная запись недоступна.",
                        null
                )
        );
    }
}
