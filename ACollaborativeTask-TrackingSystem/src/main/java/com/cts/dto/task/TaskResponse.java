package com.cts.dto.task;

import com.cts.dto.user.UserResponse;
import com.cts.entity.Task;
import com.cts.entity.User;
import com.cts.entity.enums.TaskPriority;
import com.cts.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
		Long id,
		String title,
		String description,
		TaskStatus status,
		TaskPriority priority,
		LocalDate dueDate,
		UserResponse assignee,
		UserResponse createdBy,
		Long teamId,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static TaskResponse from(Task task) {
		User assignee = task.getAssignee();
		User createdBy = task.getCreatedBy();
		return new TaskResponse(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.getStatus(),
				task.getPriority(),
				task.getDueDate(),
				assignee != null ? UserResponse.from(assignee) : null,
				createdBy != null ? UserResponse.from(createdBy) : null,
				task.getTeam() != null ? task.getTeam().getId() : null,
				task.getCreatedAt(),
				task.getUpdatedAt()
		);
	}
}