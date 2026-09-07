package com.cts.security;

import com.cts.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

	private final JwtConfig jwtConfig;
	private final SecretKey key;

	public JwtUtil(JwtConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
		this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
	}

	public String generateToken(String email) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtConfig.getExpirationMs());
		return Jwts.builder()
				.subject(email)
				.id(UUID.randomUUID().toString())
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}

	public String getEmailFromToken(String token) {
		return parseClaims(token).getSubject();
	}

	public String getTokenId(String token) {
		Claims claims = parseClaims(token);
		return claims.getId();
	}

	public Date getExpiration(String token) {
		return parseClaims(token).getExpiration();
	}

	public boolean validateToken(String token) {
		parseClaims(token);
		return true;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}