package com.cts.service;

import com.cts.entity.TokenBlacklist;
import com.cts.repository.TokenBlacklistRepository;
import com.cts.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TokenBlacklistService {

	private final TokenBlacklistRepository tokenBlacklistRepository;
	private final JwtUtil jwtUtil;

	public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository, JwtUtil jwtUtil) {
		this.tokenBlacklistRepository = tokenBlacklistRepository;
		this.jwtUtil = jwtUtil;
	}

	@Transactional
	public void revoke(String token) {
		String tokenId = jwtUtil.getTokenId(token);
		if (!tokenBlacklistRepository.existsByTokenId(tokenId)) {
			TokenBlacklist entry = new TokenBlacklist();
			entry.setTokenId(tokenId);
			entry.setEmail(jwtUtil.getEmailFromToken(token));
			Date expiresAt = jwtUtil.getExpiration(token);
			entry.setExpiresAt(LocalDateTime.ofInstant(expiresAt.toInstant(), ZoneId.systemDefault()));
			tokenBlacklistRepository.save(entry);
		}
	}

	@Transactional(readOnly = true)
	public boolean isRevoked(String tokenId) {
		return tokenBlacklistRepository.existsByTokenId(tokenId);
	}

	@Transactional
	public void purgeExpired() {
		tokenBlacklistRepository.deleteExpired(LocalDateTime.now());
	}
}
