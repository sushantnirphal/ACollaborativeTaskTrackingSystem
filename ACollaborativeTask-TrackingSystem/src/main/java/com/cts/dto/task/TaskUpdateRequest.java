package com.cts.dto.task;

import com.cts.entity.enums.TaskPriority;
import com.cts.entity.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
		@Size(max = 150, message = "Title must be at most 150 characters")
		String title,

		@Size(max = 1000, message = "Description must be at most 1000 characters")
		String description,

		TaskStatus status,

		TaskPriority priority,

		@FutureOrPresent(message = "Due date must not be in the past")
		LocalDate dueDate,

		Long assigneeId
) {
}