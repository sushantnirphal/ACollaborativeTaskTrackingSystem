package com.cts.service;

import com.cts.dto.task.TaskRequest;
import com.cts.dto.task.TaskResponse;
import com.cts.dto.task.TaskUpdateRequest;
import com.cts.entity.Task;
import com.cts.entity.Team;
import com.cts.entity.User;
import com.cts.entity.enums.TaskPriority;
import com.cts.entity.enums.TaskStatus;
import com.cts.exception.InvalidOperationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.TaskRepository;
import com.cts.repository.TeamMemberRepository;
import com.cts.repository.TeamRepository;
import com.cts.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

	private final TaskRepository taskRepository;
	private final UserRepository userRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final CurrentUser currentUser;

	public TaskService(TaskRepository taskRepository,
					   UserRepository userRepository,
					   TeamRepository teamRepository,
					   TeamMemberRepository teamMemberRepository,
					   CurrentUser currentUser) {
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
		this.teamRepository = teamRepository;
		this.teamMemberRepository = teamMemberRepository;
		this.currentUser = currentUser;
	}

	@Transactional
	public TaskResponse create(TaskRequest request) {
		User creator = currentUser.get();

		Task task = new Task();
		task.setTitle(request.title());
		task.setDescription(request.description());
		task.setStatus(request.status() != null ? request.status() : TaskStatus.TO_DO);
		task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
		task.setDueDate(request.dueDate());
		task.setCreatedBy(creator);

		if (request.assigneeId() != null) {
			User assignee = userRepository.findById(request.assigneeId())
					.orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id " + request.assigneeId()));
			task.setAssignee(assignee);
		}

		if (request.teamId() != null) {
			Team team = teamRepository.findById(request.teamId())
					.orElseThrow(() -> new ResourceNotFoundException("Team not found with id " + request.teamId()));
			assertIsMemberOf(team.getId(), creator.getId(), "You must be a team member to create tasks in this team");
			if (request.assigneeId() != null) {
				assertIsMemberOf(team.getId(), request.assigneeId(),
						"Assignee must be a member of the team");
			}
			task.setTeam(team);
		}

		return TaskResponse.from(taskRepository.save(task));
	}

	@Transactional(readOnly = true)
	public TaskResponse get(Long id) {
		Task task = findTask(id);
		assertCanAccess(task, currentUser.get());
		return TaskResponse.from(task);
	}

	@Transactional
	public TaskResponse update(Long id, TaskUpdateRequest request) {
		Task task = findTask(id);
		User current = currentUser.get();
		assertCanModify(task, current);

		if (request.title() != null && !request.title().isBlank()) {
			task.setTitle(request.title());
		}
		if (request.description() != null) {
			task.setDescription(request.description());
		}
		if (request.status() != null) {
			task.setStatus(request.status());
		}
		if (request.priority() != null) {
			task.setPriority(request.priority());
		}
		if (request.dueDate() != null) {
			task.setDueDate(request.dueDate());
		}
		if (request.assigneeId() != null) {
			User assignee = userRepository.findById(request.assigneeId())
					.orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id " + request.assigneeId()));
			if (task.getTeam() != null) {
				assertIsMemberOf(task.getTeam().getId(), assignee.getId(),
						"Assignee must be a member of the team");
			}
			task.setAssignee(assignee);
		}

		return TaskResponse.from(taskRepository.save(task));
	}

	@Transactional
	public void delete(Long id) {
		Task task = findTask(id);
		assertCanModify(task, currentUser.get());
		taskRepository.delete(task);
	}

	@Transactional(readOnly = true)
	public Page<TaskResponse> listTasksAssignedToMe(Pageable pageable) {
		return taskRepository.findByAssigneeId(currentUser.id(), pageable).map(TaskResponse::from);
	}

	@Transactional(readOnly = true)
	public Page<TaskResponse> list(TaskStatus status, String search, Long teamId, Pageable pageable) {
		Page<Task> tasks;
		if (teamId != null) {
			Team team = teamRepository.findById(teamId)
					.orElseThrow(() -> new ResourceNotFoundException("Team not found with id " + teamId));
			assertIsMemberOf(team.getId(), currentUser.id(), "You must be a team member to view team tasks");
			tasks = taskRepository.findByTeamId(teamId, pageable);
		} else if (status != null || (search != null && !search.isBlank())) {
			tasks = taskRepository.filterTasks(status, search, pageable);
		} else {
			tasks = taskRepository.findAll(pageable);
		}
		return tasks.map(TaskResponse::from);
	}

	@Transactional(readOnly = true)
	public Task getEntity(Long id) {
		Task task = findTask(id);
		assertCanAccess(task, currentUser.get());
		return task;
	}

	private Task findTask(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
	}

	public void assertCanAccess(Task task, User user) {
		boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(user.getId());
		boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(user.getId());
		boolean isTeamMember = task.getTeam() != null
				&& teamMemberRepository.existsByTeamIdAndUserId(task.getTeam().getId(), user.getId());
		if (!isCreator && !isAssignee && !isTeamMember) {
			throw new InvalidOperationException("You do not have access to this task");
		}
	}

	private void assertCanModify(Task task, User user) {
		boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(user.getId());
		boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(user.getId());
		if (!isCreator && !isAssignee) {
			throw new InvalidOperationException("Only the task creator or assignee can modify this task");
		}
	}

	private void assertIsMemberOf(Long teamId, Long userId, String message) {
		if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
			throw new InvalidOperationException(message);
		}
	}
}