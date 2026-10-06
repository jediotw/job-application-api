package com.example.jobapplicationapi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final String SECRET_KEY = "my-super-secret-key-for-job-application-api-123456789";

  private static final long EXPIRATION_TIME = 1000 * 60 * 60;

  private final SecretKey secretKey;

  public JwtService() {
    this.secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
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
