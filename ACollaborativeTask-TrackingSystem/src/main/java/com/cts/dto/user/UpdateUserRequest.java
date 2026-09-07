package com.cts.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
		@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
		String username,

		@Size(max = 50)
		String firstName,

		@Size(max = 50)
		String lastName
) {
}