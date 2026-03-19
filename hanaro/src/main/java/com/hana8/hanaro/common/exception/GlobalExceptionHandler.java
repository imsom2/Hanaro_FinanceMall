package com.hana8.hanaro.common.exception;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Order(2)
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비즈니스 로직 예외
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("BusinessException: {}", e.getMessage());
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponseDTO.of(errorCode, e.getMessage()));
	}

	// DB 무결성 제약 위반
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		log.error("DataIntegrityViolationException", e);
		String message = e.getMostSpecificCause().getMessage();

		if (message.contains("uniq_User_email")) {
			return ResponseEntity
				.status(ErrorCode.USER_EMAIL_DUPLICATE.getHttpStatus())
				.body(ErrorResponseDTO.of(ErrorCode.USER_EMAIL_DUPLICATE));
		}
		if (message.contains("uniq_Account_accountNum")) {
			return ResponseEntity
				.status(ErrorCode.ACCOUNT_NUM_DUPLICATE.getHttpStatus())
				.body(ErrorResponseDTO.of(ErrorCode.ACCOUNT_NUM_DUPLICATE));
		}

		return ResponseEntity
			.status(ErrorCode.DATABASE_ERROR.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.DATABASE_ERROR));
	}

	// 처리되지 않은 모든 예외 (500)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleException(Exception e) {
		log.error("UnhandledException: {}", e.getMessage(), e);
		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponseDTO.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
