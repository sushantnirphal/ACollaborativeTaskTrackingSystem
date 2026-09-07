package com.cts.service;

import com.cts.dto.attachment.AttachmentResponse;
import com.cts.entity.Attachment;
import com.cts.entity.Task;
import com.cts.entity.User;
import com.cts.exception.FileStorageException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

	private final AttachmentRepository attachmentRepository;
	private final TaskService taskService;
	private final CurrentUser currentUser;
	private final Path uploadDir;

	public AttachmentService(AttachmentRepository attachmentRepository,
							 TaskService taskService,
							 CurrentUser currentUser,
							 @Value("${app.upload.dir:uploads}") String uploadDir) {
		this.attachmentRepository = attachmentRepository;
		this.taskService = taskService;
		this.currentUser = currentUser;
		this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
	}

	@Transactional
	public AttachmentResponse upload(Long taskId, MultipartFile file) {
		Task task = taskService.getEntity(taskId);
		User uploader = currentUser.get();
		taskService.assertCanAccess(task, uploader);

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File must not be empty");
		}

		String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
		String storedName = UUID.randomUUID() + "-" + originalName;
		Path taskDir = uploadDir.resolve(String.valueOf(taskId));
		Path target = taskDir.resolve(storedName);

		try {
			Files.createDirectories(taskDir);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new FileStorageException("Failed to store file: " + ex.getMessage());
		}

		Attachment attachment = new Attachment();
		attachment.setFileName(originalName);
		attachment.setContentType(file.getContentType());
		attachment.setSize(file.getSize());
		attachment.setStoragePath(target.toString());
		attachment.setUploadedBy(uploader);
		attachment.setTask(task);

		return AttachmentResponse.from(attachmentRepository.save(attachment));
	}

	@Transactional(readOnly = true)
	public List<AttachmentResponse> list(Long taskId) {
		taskService.getEntity(taskId);
		return attachmentRepository.findByTaskIdOrderByUploadedAtAsc(taskId).stream()
				.map(AttachmentResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DownloadInfo download(Long attachmentId) {
		Attachment attachment = attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id " + attachmentId));
		taskService.assertCanAccess(attachment.getTask(), currentUser.get());

		Path path = Paths.get(attachment.getStoragePath());
		if (!Files.exists(path)) {
			throw new ResourceNotFoundException("Attachment file is missing on disk");
		}

		Resource resource = new PathResource(path);
		return new DownloadInfo(attachment, resource);
	}

	public record DownloadInfo(Attachment attachment, Resource resource) {
	}
}