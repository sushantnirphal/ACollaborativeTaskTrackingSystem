package com.cts.controller;

import com.cts.dto.team.AddMemberRequest;
import com.cts.dto.team.MemberResponse;
import com.cts.dto.team.TeamCreateRequest;
import com.cts.dto.team.TeamResponse;
import com.cts.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

	private final TeamService teamService;

	public TeamController(TeamService teamService) {
		this.teamService = teamService;
	}

	@PostMapping
	public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(request));
	}

	@GetMapping
	public ResponseEntity<List<TeamResponse>> list() {
		return ResponseEntity.ok(teamService.list());
	}

	@GetMapping("/{id}")
	public ResponseEntity<TeamResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(teamService.get(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TeamResponse> update(@PathVariable Long id,
											   @Valid @RequestBody TeamCreateRequest request) {
		return ResponseEntity.ok(teamService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		teamService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/members")
	public ResponseEntity<MemberResponse> addMember(@PathVariable Long id,
													@Valid @RequestBody AddMemberRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(teamService.addMember(id, request));
	}

	@DeleteMapping("/{id}/members/{userId}")
	public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
		teamService.removeMember(id, userId);
		return ResponseEntity.noContent().build();
	}
}