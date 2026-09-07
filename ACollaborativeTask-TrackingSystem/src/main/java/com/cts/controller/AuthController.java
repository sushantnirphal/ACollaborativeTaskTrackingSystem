package com.cts.controller;

import com.cts.dto.auth.AuthResponse;
import com.cts.dto.auth.LoginRequest;
import com.cts.dto.auth.RegisterRequest;
import com.cts.dto.user.UserResponse;
import com.cts.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String BEARER_PREFIX = "Bearer ";

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization") String authorizationHeader) {
		if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
			authService.logout(authorizationHeader.substring(BEARER_PREFIX.length()));
		}
		return ResponseEntity.noContent().build();
	}
}
