package com.cts.controller;

import com.cts.dto.user.UpdateUserRequest;
import com.cts.dto.user.UserResponse;
import com.cts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> me() {
		return ResponseEntity.ok(userService.me());
	}

	@PutMapping("/me")
	public ResponseEntity<UserResponse> update(@Valid @RequestBody UpdateUserRequest request) {
		return ResponseEntity.ok(userService.update(request));
	}
}