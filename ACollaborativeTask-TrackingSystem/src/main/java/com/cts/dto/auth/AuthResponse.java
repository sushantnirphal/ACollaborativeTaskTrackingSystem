package com.cts.dto.auth;

import com.cts.dto.user.UserResponse;

public record AuthResponse(
		String accessToken,
		String tokenType,
		UserResponse user
) {
}