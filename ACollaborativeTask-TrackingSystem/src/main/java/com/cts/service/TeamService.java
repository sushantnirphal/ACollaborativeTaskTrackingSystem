package com.cts.service;

import com.cts.dto.team.AddMemberRequest;
import com.cts.dto.team.MemberResponse;
import com.cts.dto.team.TeamCreateRequest;
import com.cts.dto.team.TeamResponse;
import com.cts.entity.Team;
import com.cts.entity.TeamMember;
import com.cts.entity.User;
import com.cts.entity.enums.Role;
import com.cts.exception.DuplicateResourceException;
import com.cts.exception.InvalidOperationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.TaskRepository;
import com.cts.repository.TeamMemberRepository;
import com.cts.repository.TeamRepository;
import com.cts.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final UserRepository userRepository;
	private final TaskRepository taskRepository;
	private final CurrentUser currentUser;

	public TeamService(TeamRepository teamRepository,
					   TeamMemberRepository teamMemberRepository,
					   UserRepository userRepository,
					   TaskRepository taskRepository,
					   CurrentUser currentUser) {
		this.teamRepository = teamRepository;
		this.teamMemberRepository = teamMemberRepository;
		this.userRepository = userRepository;
		this.taskRepository = taskRepository;
		this.currentUser = currentUser;
	}

	@Transactional
	public TeamResponse create(TeamCreateRequest request) {
		User owner = currentUser.get();

		Team team = new Team();
		team.setName(request.name());
		team.setDescription(request.description());
		team.setOwner(owner);

		TeamMember ownerMembership = new TeamMember();
		ownerMembership.setUser(owner);
		ownerMembership.setRole(Role.OWNER);
		team.addMember(ownerMembership);

		return TeamResponse.from(teamRepository.save(team));
	}

	@Transactional(readOnly = true)
	public List<TeamResponse> list() {
		return teamRepository.findAllByMemberId(currentUser.id()).stream()
				.map(TeamResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public TeamResponse get(Long id) {
		Team team = getTeam(id);
		assertIsMember(team.getId(), "You do not have access to this team");
		return TeamResponse.from(team);
	}

	@Transactional
	public TeamResponse update(Long id, TeamCreateRequest request) {
		Team team = getTeam(id);
		assertIsOwner(team);

		team.setName(request.name());
		team.setDescription(request.description());
		return TeamResponse.from(teamRepository.save(team));
	}

	@Transactional
	public void delete(Long id) {
		Team team = getTeam(id);
		assertIsOwner(team);

		if (taskRepository.findByTeamId(id, org.springframework.data.domain.PageRequest.of(0, 1)).hasContent()) {
			throw new InvalidOperationException("Cannot delete a team that still has tasks");
		}

		teamRepository.delete(team);
	}

	@Transactional
	public MemberResponse addMember(Long teamId, AddMemberRequest request) {
		Team team = getTeam(teamId);
		assertIsOwner(team);

		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email " + request.email()));

		if (teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
			throw new DuplicateResourceException("User is already a member of this team");
		}

		TeamMember membership = new TeamMember();
		membership.setTeam(team);
		membership.setUser(user);
		membership.setRole(Role.MEMBER);
		teamMemberRepository.save(membership);

		return MemberResponse.from(membership);
	}

	@Transactional
	public void removeMember(Long teamId, Long userId) {
		Team team = getTeam(teamId);
		assertIsOwner(team);

		if (team.getOwner().getId().equals(userId)) {
			throw new InvalidOperationException("The team owner cannot be removed");
		}

		TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found in this team"));
		teamMemberRepository.delete(membership);
	}

	@Transactional(readOnly = true)
	public Team getEntity(Long id) {
		Team team = getTeam(id);
		assertIsMember(team.getId(), "You do not have access to this team");
		return team;
	}

	private Team getTeam(Long id) {
		return teamRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Team not found with id " + id));
	}

	private void assertIsOwner(Team team) {
		if (!team.getOwner().getId().equals(currentUser.id())) {
			throw new InvalidOperationException("Only the team owner can perform this action");
		}
	}

	private void assertIsMember(Long teamId, String message) {
		if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, currentUser.id())) {
			throw new InvalidOperationException(message);
		}
	}
}