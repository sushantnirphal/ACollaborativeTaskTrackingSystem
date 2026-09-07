package com.cts.service;

import com.cts.dto.auth.AuthResponse;
import com.cts.dto.auth.LoginRequest;
import com.cts.dto.auth.RegisterRequest;
import com.cts.dto.user.UserResponse;
import com.cts.entity.User;
import com.cts.exception.DuplicateResourceException;
import com.cts.exception.InvalidOperationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final TokenBlacklistService tokenBlacklistService;

	public AuthService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder,
					   JwtUtil jwtUtil,
					   TokenBlacklistService tokenBlacklistService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	@Transactional
	public UserResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateResourceException("Email is already registered");
		}
		if (userRepository.existsByUsername(request.username())) {
			throw new DuplicateResourceException("Username is already taken");
		}

		User user = new User();
		user.setEmail(request.email());
		user.setUsername(request.username());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());

		return UserResponse.from(userRepository.save(user));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new InvalidOperationException("Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidOperationException("Invalid email or password");
		}

		String token = jwtUtil.generateToken(user.getEmail());
		return new AuthResponse(token, "Bearer", UserResponse.from(user));
	}

	public void logout(String token) {
		tokenBlacklistService.revoke(token);
	}
}