package com.cts.controller;

import com.cts.dto.task.TaskRequest;
import com.cts.dto.task.TaskResponse;
import com.cts.dto.task.TaskUpdateRequest;
import com.cts.entity.enums.TaskStatus;
import com.cts.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
	}

	@GetMapping
	public ResponseEntity<Page<TaskResponse>> list(
			@RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Long teamId,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(taskService.list(status, search, teamId, pageable));
	}

	@GetMapping("/assigned-to-me")
	public ResponseEntity<Page<TaskResponse>> assignedToMe(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(taskService.listTasksAssignedToMe(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(taskService.get(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TaskResponse> update(@PathVariable Long id,
											   @Valid @RequestBody TaskUpdateRequest request) {
		return ResponseEntity.ok(taskService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		taskService.delete(id);
		return ResponseEntity.noContent().build();
	}
}