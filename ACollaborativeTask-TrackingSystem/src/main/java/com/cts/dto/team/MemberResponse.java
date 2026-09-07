package com.cts.dto.team;

import com.cts.entity.TeamMember;
import com.cts.entity.enums.Role;

import java.time.LocalDateTime;

public record MemberResponse(
		Long id,
		String email,
		String username,
		String firstName,
		String lastName,
		Role role,
		LocalDateTime joinedAt
) {

	public static MemberResponse from(TeamMember membership) {
		return new MemberResponse(
				membership.getUser().getId(),
				membership.getUser().getEmail(),
				membership.getUser().getUsername(),
				membership.getUser().getFirstName(),
				membership.getUser().getLastName(),
				membership.getRole(),
				membership.getJoinedAt()
		);
	}
}