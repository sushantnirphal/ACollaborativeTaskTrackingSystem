package com.cts.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist", indexes = {
		@Index(name = "idx_token_id", columnList = "token_id", unique = true)
})
public class TokenBlacklist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "token_id", nullable = false, unique = true, length = 100)
	private String tokenId;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "revoked_at", nullable = false, updatable = false)
	private LocalDateTime revokedAt;

	@PrePersist
	protected void onCreate() {
		revokedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public LocalDateTime getRevokedAt() {
		return revokedAt;
	}
}
