package com.cts.service;

import com.cts.dto.user.UpdateUserRequest;
import com.cts.dto.user.UserResponse;
import com.cts.entity.User;
import com.cts.exception.DuplicateResourceException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final CurrentUser currentUser;

	public UserService(UserRepository userRepository, CurrentUser currentUser) {
		this.userRepository = userRepository;
		this.currentUser = currentUser;
	}

	@Transactional(readOnly = true)
	public UserResponse me() {
		return UserResponse.from(currentUser.get());
	}

	@Transactional
	public UserResponse update(UpdateUserRequest request) {
		User user = currentUser.get();

		if (request.username() != null && !request.username().isBlank()
				&& userRepository.existsByUsername(request.username())
				&& !user.getUsername().equals(request.username())) {
			throw new DuplicateResourceException("Username is already taken");
		}

		if (request.username() != null && !request.username().isBlank()) {
			user.setUsername(request.username());
		}
		if (request.firstName() != null) {
			user.setFirstName(request.firstName());
		}
		if (request.lastName() != null) {
			user.setLastName(request.lastName());
		}

		return UserResponse.from(userRepository.save(user));
	}

	@Transactional(readOnly = true)
	public User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
	}
}