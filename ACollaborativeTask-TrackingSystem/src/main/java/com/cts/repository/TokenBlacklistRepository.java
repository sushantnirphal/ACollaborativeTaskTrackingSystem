package com.cts.repository;

import com.cts.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

	boolean existsByTokenId(String tokenId);

	@Modifying
	@Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
	int deleteExpired(LocalDateTime now);
}
