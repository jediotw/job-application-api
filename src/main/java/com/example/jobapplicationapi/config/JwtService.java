package com.example.jobapplicationapi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final long EXPIRATION_TIME = 1000 * 60 * 60;

  private final SecretKey secretKey;

  public JwtService(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(Long userId, String email, String role) {

    Date issuedAt = new Date();

    Date expiration = new Date(issuedAt.getTime() + EXPIRATION_TIME);

    return Jwts.builder()
        .subject(email)
        .claim("userId", userId)
        .claim("role", role)
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  public String extractEmail(String token) {

    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

    return claims.getSubject();
  }
}
