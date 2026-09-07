package com.cts.service;

import com.cts.dto.task.TaskRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private TeamMemberRepository teamMemberRepository;

	@Mock
	private CurrentUser currentUser;

	private TaskService taskService;

	private User creator;

	@BeforeEach
	void setUp() {
		taskService = new TaskService(taskRepository, userRepository, teamRepository,
				teamMemberRepository, currentUser);
		creator = new User();
		creator.setId(10L);
		creator.setEmail("creator@example.com");
		creator.setUsername("creator");
	}

	@Test
	void create_taskWithoutTeam_savesWithDefaults() {
		when(currentUser.get()).thenReturn(creator);
		TaskRequest request = new TaskRequest("Write report", "Q3 report", null, null,
				null, null, null);

		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
			Task task = invocation.getArgument(0);
			task.setId(1L);
			return task;
		});

		var response = taskService.create(request);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Write report");
		assertThat(response.status()).isEqualTo(TaskStatus.TO_DO);
		assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
		verify(taskRepository).save(any(Task.class));
	}

	@Test
	void create_taskWithTeamAndNotMember_throws() {
		when(currentUser.get()).thenReturn(creator);

		Team team = new Team();
		team.setId(5L);
		team.setName("Engineering");
		team.setOwner(creator);

		TaskRequest request = new TaskRequest("Fix bug", null, null, null, null, null, 5L);

		when(teamRepository.findById(5L)).thenReturn(Optional.of(team));
		when(teamMemberRepository.existsByTeamIdAndUserId(5L, 10L)).thenReturn(false);

		assertThatThrownBy(() -> taskService.create(request))
				.isInstanceOf(InvalidOperationException.class);
	}

	@Test
	void create_taskWithTeamAndMember_saves() {
		when(currentUser.get()).thenReturn(creator);

		Team team = new Team();
		team.setId(5L);
		team.setName("Engineering");
		team.setOwner(creator);

		TaskRequest request = new TaskRequest("Fix bug", null, null, null, null, null, 5L);

		when(teamRepository.findById(5L)).thenReturn(Optional.of(team));
		when(teamMemberRepository.existsByTeamIdAndUserId(5L, 10L)).thenReturn(true);
		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
			Task task = invocation.getArgument(0);
			task.setId(1L);
			return task;
		});

		var response = taskService.create(request);

		assertThat(response.teamId()).isEqualTo(5L);
	}

	@Test
	void update_taskByNonCreator_throws() {
		User other = new User();
		other.setId(30L);

		Task task = new Task();
		task.setId(1L);
		task.setTitle("Original");
		task.setCreatedBy(creator);

		when(currentUser.get()).thenReturn(other);
		when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

		TaskUpdateRequest request = new TaskUpdateRequest("Edited", null, TaskStatus.DONE, null, null, null);

		assertThatThrownBy(() -> taskService.update(1L, request))
				.isInstanceOf(InvalidOperationException.class)
				.hasMessageContaining("creator or assignee");
	}

	@Test
	void update_taskByCreator_updatesFields() {
		when(currentUser.get()).thenReturn(creator);

		Task task = new Task();
		task.setId(1L);
		task.setTitle("Original");
		task.setCreatedBy(creator);
		task.setStatus(TaskStatus.TO_DO);

		when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TaskUpdateRequest request = new TaskUpdateRequest("Edited", null, TaskStatus.DONE, TaskPriority.HIGH, null, null);

		var response = taskService.update(1L, request);

		assertThat(response.title()).isEqualTo("Edited");
		assertThat(response.status()).isEqualTo(TaskStatus.DONE);
		assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
	}

	@Test
	void get_unknownTask_throws() {
		when(taskRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.get(99L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void get_taskNotAccessible_throws() {
		User other = new User();
		other.setId(20L);
		other.setEmail("other@example.com");
		other.setUsername("other");

		Task task = new Task();
		task.setId(1L);
		task.setTitle("Secret");
		task.setCreatedBy(other);

		when(currentUser.get()).thenReturn(creator);
		when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

		assertThatThrownBy(() -> taskService.get(1L))
				.isInstanceOf(InvalidOperationException.class);
	}
}