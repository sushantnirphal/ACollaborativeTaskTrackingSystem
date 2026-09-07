package com.cts.repository;

import com.cts.entity.TeamMember;
import com.cts.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

	Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

	long countByTeamId(Long teamId);

	boolean existsByTeamIdAndUserId(Long teamId, Long userId);

	Optional<TeamMember> findFirstByTeamIdAndRole(Long teamId, Role role);
}