package com.cts.service;

import com.cts.dto.auth.AuthResponse;
import com.cts.dto.auth.LoginRequest;
import com.cts.dto.auth.RegisterRequest;
import com.cts.entity.User;
import com.cts.exception.DuplicateResourceException;
import com.cts.exception.InvalidOperationException;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private TokenBlacklistService tokenBlacklistService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, passwordEncoder, jwtUtil, tokenBlacklistService);
	}

	@Test
	void register_createsUserWithEncodedPassword() {
		RegisterRequest request = new RegisterRequest("alice@example.com", "alice", "secret123", "Alice", "Smith");
		User saved = new User();
		saved.setId(1L);
		saved.setEmail(request.email());
		saved.setUsername(request.username());
		saved.setPasswordHash("encoded-hash");
		saved.setFirstName(request.firstName());
		saved.setLastName(request.lastName());

		when(userRepository.existsByEmail(request.email())).thenReturn(false);
		when(userRepository.existsByUsername(request.username())).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("encoded-hash");
		when(userRepository.save(any(User.class))).thenReturn(saved);

		var response = authService.register(request);

		assertThat(response.email()).isEqualTo("alice@example.com");
		assertThat(response.username()).isEqualTo("alice");
		verify(passwordEncoder).encode("secret123");
		verify(userRepository).save(any(User.class));
	}

	@Test
	void register_duplicateEmail_throws() {
		RegisterRequest request = new RegisterRequest("alice@example.com", "alice", "secret123", null, null);
		when(userRepository.existsByEmail(request.email())).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateResourceException.class);

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void login_validCredentials_returnsToken() {
		LoginRequest request = new LoginRequest("alice@example.com", "secret123");
		User user = new User();
		user.setId(1L);
		user.setEmail(request.email());
		user.setUsername("alice");
		user.setPasswordHash("encoded-hash");

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(eq("secret123"), eq("encoded-hash"))).thenReturn(true);
		when(jwtUtil.generateToken(request.email())).thenReturn("jwt-token");

		AuthResponse response = authService.login(request);

		assertThat(response.accessToken()).isEqualTo("jwt-token");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.user().email()).isEqualTo("alice@example.com");
	}

	@Test
	void login_wrongPassword_throws() {
		LoginRequest request = new LoginRequest("alice@example.com", "wrong");
		User user = new User();
		user.setId(1L);
		user.setEmail(request.email());
		user.setPasswordHash("encoded-hash");

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "encoded-hash")).thenReturn(false);

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidOperationException.class);
	}

	@Test
	void login_unknownEmail_throws() {
		LoginRequest request = new LoginRequest("missing@example.com", "secret123");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidOperationException.class);
	}
}