package com.cts.dto.comment;

import com.cts.dto.user.UserResponse;
import com.cts.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
		Long id,
		String content,
		UserResponse author,
		Long taskId,
		LocalDateTime createdAt
) {

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getContent(),
				UserResponse.from(comment.getAuthor()),
				comment.getTask().getId(),
				comment.getCreatedAt()
		);
	}
}