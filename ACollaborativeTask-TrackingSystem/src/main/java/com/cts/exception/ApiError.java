package com.cts.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		List<FieldErrorDetail> fieldErrors
) {

	public record FieldErrorDetail(String field, String message) {
	}

	public static ApiError of(int status, String error, String message, String path) {
		return new ApiError(LocalDateTime.now(), status, error, message, path, null);
	}

	public static ApiError of(int status, String error, String message, String path,
							  List<FieldErrorDetail> fieldErrors) {
		return new ApiError(LocalDateTime.now(), status, error, message, path, fieldErrors);
	}
}