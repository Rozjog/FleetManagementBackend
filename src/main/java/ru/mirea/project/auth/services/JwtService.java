package ru.mirea.project.auth.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import ru.mirea.project.account.models.UserAccount;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long accessTokenMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${ACCESS_TOKEN_MINUTES:15}")
            long accessTokenMinutes
    ) {
        this.jwtEncoder = jwtEncoder;

        if (accessTokenMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Некорректный срок действия JWT."
            );
        }

        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(UserAccount account) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(account.getId().toString())
                .claim("role", account.getRole().name())
                .claim("temporary", account.isMustChangePassword())
                .expiresAt(now.plusSeconds(getExpiresIn()))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public long getExpiresIn() {
        return accessTokenMinutes * 60;
    }
}
