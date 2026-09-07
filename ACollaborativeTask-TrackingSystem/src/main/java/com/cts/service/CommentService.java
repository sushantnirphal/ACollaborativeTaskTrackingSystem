package com.cts.service;

import com.cts.dto.comment.CommentRequest;
import com.cts.dto.comment.CommentResponse;
import com.cts.entity.Comment;
import com.cts.entity.Task;
import com.cts.entity.User;
import com.cts.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

	private final CommentRepository commentRepository;
	private final TaskService taskService;
	private final CurrentUser currentUser;

	public CommentService(CommentRepository commentRepository,
						  TaskService taskService,
						  CurrentUser currentUser) {
		this.commentRepository = commentRepository;
		this.taskService = taskService;
		this.currentUser = currentUser;
	}

	@Transactional
	public CommentResponse add(Long taskId, CommentRequest request) {
		Task task = taskService.getEntity(taskId);
		User author = currentUser.get();
		taskService.assertCanAccess(task, author);

		Comment comment = new Comment();
		comment.setContent(request.content());
		comment.setAuthor(author);
		comment.setTask(task);

		return CommentResponse.from(commentRepository.save(comment));
	}

	@Transactional(readOnly = true)
	public List<CommentResponse> list(Long taskId) {
		taskService.getEntity(taskId);
		return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
				.map(CommentResponse::from)
				.toList();
	}
}