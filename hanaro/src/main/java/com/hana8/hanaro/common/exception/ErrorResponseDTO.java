package com.hana8.hanaro.common.exception;

import lombok.Builder;
import lombok.Data;

import org.springframework.http.HttpStatus;

@Data
@Builder
public class ErrorResponseDTO {
	private final boolean isSuccess;
	private final int status;
	private final String code;
	private final String message;

	public static ErrorResponseDTO of(ErrorCode errorCode) {
		return ErrorResponseDTO.builder()
			.isSuccess(false)
			.status(errorCode.getHttpStatus().value())
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.build();
	}

	public static ErrorResponseDTO of(ErrorCode errorCode, String message) {
		return ErrorResponseDTO.builder()
			.isSuccess(false)
			.status(errorCode.getHttpStatus().value())
			.code(errorCode.getCode())
			.message(message)
			.build();
	}
}
