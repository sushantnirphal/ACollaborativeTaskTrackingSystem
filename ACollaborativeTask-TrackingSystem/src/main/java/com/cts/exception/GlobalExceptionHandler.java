package com.cts.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return respond(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, null);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
		return respond(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, null);
	}

	@ExceptionHandler(InvalidOperationException.class)
	public ResponseEntity<ApiError> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
		return respond(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, null);
	}

	@ExceptionHandler(FileStorageException.class)
	public ResponseEntity<ApiError> handleFileStorage(FileStorageException ex, HttpServletRequest request) {
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request, null);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		return respond(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<ApiError.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new ApiError.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
				.collect(Collectors.toList());
		return respond(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed", request, fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
		return respond(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed request body", request, null);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
													   HttpServletRequest request) {
		return respond(HttpStatus.BAD_REQUEST, "Bad Request",
				"Invalid value for parameter '" + ex.getName() + "'", request, null);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
															 HttpServletRequest request) {
		return respond(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(), request, null);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
		return respond(HttpStatus.NOT_FOUND, "Not Found", "Resource not found", request, null);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex,
														HttpServletRequest request) {
		return respond(HttpStatus.BAD_REQUEST, "Bad Request", "File exceeds maximum upload size", request, null);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		return respond(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to perform this action",
				request, null);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
		return respond(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request, null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
				"An unexpected error occurred", request, null);
	}

	private ResponseEntity<ApiError> respond(HttpStatus status, String error, String message,
											 HttpServletRequest request, List<ApiError.FieldErrorDetail> fieldErrors) {
		return ResponseEntity.status(status)
				.body(ApiError.of(status.value(), error, message, request.getRequestURI(), fieldErrors));
	}
}