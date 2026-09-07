package com.cts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.expiration-ms}")
	private long expirationMs;

	public String getSecret() {
		return secret;
	}

	public long getExpirationMs() {
		return expirationMs;
	}
}