package com.hana8.hanaro.common.exception;

import com.hana8.hanaro.security.exception.CustomJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
		log.warn("AccessDenied: {}", e.getMessage());
		return ResponseEntity
			.status(ErrorCode.ACCESS_DENIED.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.ACCESS_DENIED));
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("BusinessException: {}", e.getMessage());
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponseDTO.of(errorCode, e.getMessage()));
	}

	// Validation 실패 (400)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(FieldError::getDefaultMessage)
			.collect(Collectors.joining(", "));
		log.warn("ValidationException: {}", message);
		return ResponseEntity
			.status(ErrorCode.INVALID_INPUT.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.INVALID_INPUT, message));
	}

	// JWT 예외 (401)
	@ExceptionHandler(CustomJwtException.class)
	public ResponseEntity<ErrorResponseDTO> handleJwtException(CustomJwtException e) {
		log.warn("JwtException: {}", e.getMessage());
		ErrorCode errorCode = e.getMessage().contains("Expired")
			? ErrorCode.JWT_EXPIRED
			: ErrorCode.JWT_INVALID;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponseDTO.of(errorCode));
	}

	// 서버 에러 (500)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleException(Exception e) {
		log.error("UnhandledException: {}", e.getMessage(), e);
		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
