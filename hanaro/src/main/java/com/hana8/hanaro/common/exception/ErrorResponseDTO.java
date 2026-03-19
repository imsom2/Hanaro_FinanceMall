package com.hana8.hanaro.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import org.springframework.http.HttpStatus;

@Data
@Builder
@Schema(description = "공통 에러 응답 DTO")
public class ErrorResponseDTO {
	private final boolean isSuccess;

	@Schema(description = "HTTP 상태 코드")
	private final int status;

	@Schema(description = "응답 에러 코드")
	private final String code;

	@Schema(description = "응답 에러 메시지")
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
