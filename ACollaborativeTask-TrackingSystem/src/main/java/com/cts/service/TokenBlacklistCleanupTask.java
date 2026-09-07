package com.cts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklistCleanupTask {

	private static final Logger log = LoggerFactory.getLogger(TokenBlacklistCleanupTask.class);

	private final TokenBlacklistService tokenBlacklistService;

	public TokenBlacklistCleanupTask(TokenBlacklistService tokenBlacklistService) {
		this.tokenBlacklistService = tokenBlacklistService;
	}

	@Scheduled(cron = "0 0 */6 * * *")
	public void purgeExpiredTokens() {
		tokenBlacklistService.purgeExpired();
		log.info("Purged expired JWT blacklist entries");
	}
}
