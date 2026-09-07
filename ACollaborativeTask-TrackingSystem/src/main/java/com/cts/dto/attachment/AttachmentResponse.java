package com.cts.dto.attachment;

import com.cts.entity.Attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
		Long id,
		String fileName,
		String contentType,
		long size,
		Long uploadedById,
		String uploadedByUsername,
		Long taskId,
		LocalDateTime uploadedAt
) {

	public static AttachmentResponse from(Attachment attachment) {
		return new AttachmentResponse(
				attachment.getId(),
				attachment.getFileName(),
				attachment.getContentType(),
				attachment.getSize(),
				attachment.getUploadedBy().getId(),
				attachment.getUploadedBy().getUsername(),
				attachment.getTask().getId(),
				attachment.getUploadedAt()
		);
	}
}