package com.cts.controller;

import com.cts.dto.attachment.AttachmentResponse;
import com.cts.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class AttachmentController {

	private final AttachmentService attachmentService;

	public AttachmentController(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@PostMapping("/api/tasks/{taskId}/attachments")
	public ResponseEntity<AttachmentResponse> upload(@PathVariable Long taskId,
													 @RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(attachmentService.upload(taskId, file));
	}

	@GetMapping("/api/tasks/{taskId}/attachments")
	public ResponseEntity<List<AttachmentResponse>> list(@PathVariable Long taskId) {
		return ResponseEntity.ok(attachmentService.list(taskId));
	}

	@GetMapping("/api/attachments/{attachmentId}/download")
	public ResponseEntity<Resource> download(@PathVariable Long attachmentId) {
		AttachmentService.DownloadInfo downloadInfo = attachmentService.download(attachmentId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(
						resolveMediaType(downloadInfo.attachment().getContentType())))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + downloadInfo.attachment().getFileName() + "\"")
				.body(downloadInfo.resource());
	}

	private String resolveMediaType(String contentType) {
		return contentType != null && !contentType.isBlank()
				? contentType
				: MediaType.APPLICATION_OCTET_STREAM_VALUE;
	}
}