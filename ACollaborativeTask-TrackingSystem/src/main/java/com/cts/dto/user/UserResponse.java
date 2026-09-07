package com.cts.dto.user;

import com.cts.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
		Long id,
		String email,
		String username,
		String firstName,
		String lastName,
		LocalDateTime createdAt
) {

	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getUsername(),
				user.getFirstName(),
				user.getLastName(),
				user.getCreatedAt()
		);
	}
}