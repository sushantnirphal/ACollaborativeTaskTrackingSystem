package com.cts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:flowtest;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE")
class CollaborationFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Test
	void fullCollaborationFlow() throws Exception {
		String ownerEmail = "lead@test.com";
		String memberEmail = "dev@test.com";

		long ownerId = register(ownerEmail, "lead", "password123");
		long memberId = register(memberEmail, "dev", "password123");

		String ownerToken = loginToken(ownerEmail, "password123");
		String memberToken = loginToken(memberEmail, "password123");

		long teamId = createTeam(ownerToken, "Platform Team", "Backend squad");

		mockMvc.perform(post("/api/teams/" + teamId + "/members")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + memberEmail + "\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("MEMBER"))
				.andExpect(jsonPath("$.id").value((int) memberId));

		long taskId = createTask(ownerToken,
				Map.of("title", "Fix login bug",
						"description", "Session token not persisted",
						"priority", "HIGH",
						"status", "TO_DO",
						"assigneeId", memberId,
						"teamId", teamId));

		mockMvc.perform(get("/api/tasks/assigned-to-me")
						.header("Authorization", "Bearer " + memberToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].id").value((int) taskId));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.put("/api/tasks/" + taskId)
						.header("Authorization", "Bearer " + memberToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DONE\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DONE"));

		mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
						.header("Authorization", "Bearer " + memberToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"content\":\"I marked this done.\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.author.id").value((int) memberId));

		mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(multipart("/api/tasks/" + taskId + "/attachments")
						.file(new MockMultipartFile("file", "notes.txt", "text/plain", "hello world".getBytes()))
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fileName").value("notes.txt"));

		mockMvc.perform(get("/api/tasks/" + taskId + "/attachments")
						.header("Authorization", "Bearer " + memberToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(post("/api/teams/" + teamId + "/members")
						.header("Authorization", "Bearer " + memberToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"someone-else@test.com\"}"))
				.andExpect(status().isConflict());

		mockMvc.perform(delete("/api/teams/" + teamId + "/members/" + memberId)
						.header("Authorization", "Bearer " + memberToken))
				.andExpect(status().isConflict());

		mockMvc.perform(delete("/api/teams/" + teamId + "/members/" + memberId)
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/teams/" + teamId)
						.header("Authorization", "Bearer " + memberToken))
				.andExpect(status().isConflict());
	}

	private long register(String email, String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"email", email, "username", username, "password", password))))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private String loginToken(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
	}

	private long createTeam(String token, String name, String description) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/teams")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("name", name, "description", description))))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private long createTask(String token, Map<String, Object> payload) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/tasks")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}
}