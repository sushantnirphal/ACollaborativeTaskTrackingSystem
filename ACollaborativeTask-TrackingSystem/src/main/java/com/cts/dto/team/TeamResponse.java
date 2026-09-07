package com.cts.dto.team;

import com.cts.dto.user.UserResponse;
import com.cts.entity.Team;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
		Long id,
		String name,
		String description,
		UserResponse owner,
		List<MemberResponse> members,
		LocalDateTime createdAt
) {

	public static TeamResponse from(Team team) {
		return new TeamResponse(
				team.getId(),
				team.getName(),
				team.getDescription(),
				UserResponse.from(team.getOwner()),
				team.getMembers().stream().map(MemberResponse::from).toList(),
				team.getCreatedAt()
		);
	}
}