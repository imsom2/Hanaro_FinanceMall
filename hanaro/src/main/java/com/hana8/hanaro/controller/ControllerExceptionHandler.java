package com.hana8.hanaro.controller;

import java.util.stream.Collectors;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.common.exception.ErrorResponseDTO;
import com.hana8.hanaro.security.exception.CustomJwtException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(1)
@RestControllerAdvice
public class ControllerExceptionHandler {

	// @Valid 검증 실패 (RequestBody 필드 유효성)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.map(FieldError::getDefaultMessage)
			.collect(Collectors.joining(", "));
		log.warn("ValidationException: {}", message);
		return ResponseEntity
			.status(ErrorCode.INVALID_INPUT.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.INVALID_INPUT, message));
	}

	// @Validated 검증 실패 (PathVariable, RequestParam 유효성)
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException e) {
		String message = e.getConstraintViolations().stream()
			.map(v -> v.getPropertyPath() + ": " + v.getMessage())
			.collect(Collectors.joining(", "));
		log.warn("ConstraintViolationException: {}", message);
		return ResponseEntity
			.status(ErrorCode.INVALID_INPUT.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.INVALID_INPUT, message));
	}

	// 존재하지 않는 엔드포인트 (404)
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleNoHandlerFound(NoHandlerFoundException e) {
		log.warn("NoHandlerFound: {} {}", e.getHttpMethod(), e.getRequestURL());
		return ResponseEntity
			.status(ErrorCode.ENDPOINT_NOT_FOUND.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.ENDPOINT_NOT_FOUND,
				e.getHttpMethod() + " " + e.getRequestURL()));
	}

	// 접근 거부 - Spring Security filterChain 수준 (403)
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException e) {
		log.warn("AccessDenied: {}", e.getMessage());
		return ResponseEntity
			.status(ErrorCode.ACCESS_DENIED.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.ACCESS_DENIED));
	}

	// 접근 거부 - @PreAuthorize 등 메서드 시큐리티 수준 (403)
	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<ErrorResponseDTO> handleAuthorizationDenied(AuthorizationDeniedException e) {
		log.warn("AuthorizationDenied: {}", e.getMessage());
		return ResponseEntity
			.status(ErrorCode.ACCESS_DENIED.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.ACCESS_DENIED));
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
}
