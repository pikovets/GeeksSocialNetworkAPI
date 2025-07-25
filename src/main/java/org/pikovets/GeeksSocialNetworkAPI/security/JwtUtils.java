package org.pikovets.GeeksSocialNetworkAPI.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JwtUtils {

    @Value("${jwt.jwtSecret}")
    private String jwtSecret;

    private final UserService userService;

    public JwtUtils(UserService userService) {
        this.userService = userService;
    }

    public Mono<String> generateToken(Map<String, Object> extraClaims, Mono<User> userMono) {
        return userMono.map(user ->
                Jwts.builder()
                        .setClaims(extraClaims)
                        .setSubject(user.getId().toString())
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14)))
                        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                        .compact()
        );
    }


    public Mono<String> generateToken(Mono<User> userMono) {
        return generateToken(new HashMap<>(), userMono);
    }

    public Mono<Boolean> isTokenValid(String token) {
        if (!StringUtils.hasText(token)) {
            return Mono.just(Boolean.FALSE);
        }

        String userId;
        try {
            userId = extractUsername(token);
            if (!StringUtils.hasText(userId) || isTokenExpired(token)) {
                return Mono.just(Boolean.FALSE);
            }
            UUID uuid = UUID.fromString(userId);
            return userService.getUserById(uuid)
                    .map(user -> true)
                    .onErrorResume(e -> Mono.just(false));
        } catch (Exception e) {
            return Mono.just(Boolean.FALSE);
        }
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    }
}
