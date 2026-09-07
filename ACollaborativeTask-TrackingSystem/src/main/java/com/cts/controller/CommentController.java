package com.cts.controller;

import com.cts.dto.comment.CommentRequest;
import com.cts.dto.comment.CommentResponse;
import com.cts.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@PostMapping
	public ResponseEntity<CommentResponse> add(@PathVariable Long taskId,
											   @Valid @RequestBody CommentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commentService.add(taskId, request));
	}

	@GetMapping
	public ResponseEntity<List<CommentResponse>> list(@PathVariable Long taskId) {
		return ResponseEntity.ok(commentService.list(taskId));
	}
}