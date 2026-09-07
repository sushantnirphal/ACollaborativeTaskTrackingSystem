package com.cts.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 100)
		String email,

		@NotBlank(message = "Username is required")
		@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
		String username,

		@NotBlank(message = "Password is required")
		@Size(min = 6, max = 100, message = "Password must be at least 6 characters")
		String password,

		@Size(max = 50)
		String firstName,

		@Size(max = 50)
		String lastName
) {
}