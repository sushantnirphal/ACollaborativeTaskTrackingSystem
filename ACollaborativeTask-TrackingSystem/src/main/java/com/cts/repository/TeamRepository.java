package com.cts.repository;

import com.cts.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

	@Query("select t from Team t join t.members m where m.user.id = :userId")
	List<Team> findAllByMemberId(@Param("userId") Long userId);

	@Query("select t from Team t where t.owner.id = :userId")
	List<Team> findAllByOwnerId(@Param("userId") Long userId);
}