package com.cts.repository;

import com.cts.entity.Task;
import com.cts.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

	Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

	Page<Task> findByAssigneeIdAndStatus(Long assigneeId, TaskStatus status, Pageable pageable);

	Page<Task> findByCreatedById(Long userId, Pageable pageable);

	Page<Task> findByTeamId(Long teamId, Pageable pageable);

	@Query("select t from Task t where lower(t.title) like lower(concat('%', :q, '%')) " +
			"or lower(t.description) like lower(concat('%', :q, '%'))")
	Page<Task> search(@Param("q") String query, Pageable pageable);

	@Query("select t from Task t where " +
			"(:status is null or t.status = :status) and " +
			"(:q is null or lower(t.title) like lower(concat('%', :q, '%')) " +
			"or lower(t.description) like lower(concat('%', :q, '%')))")
	Page<Task> filterTasks(@Param("status") TaskStatus status,
						   @Param("q") String query,
						   Pageable pageable);

	List<Task> findByDueDateBefore(LocalDate date);
}