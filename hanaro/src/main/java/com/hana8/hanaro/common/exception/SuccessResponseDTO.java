package com.hana8.hanaro.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "공통 성공 응답 DTO")
public class SuccessResponseDTO<T> {
	private final boolean isSuccess = true;
	@Schema(description = "응답 코드")
	private final String code;

	@Schema(description = "응답 메시지")
	private final String message;

	@Schema(description = "응답 데이터")
	private final T data;

	public static <T> SuccessResponseDTO<T> of(SuccessCode code, T data) {
		return SuccessResponseDTO.<T>builder()
			.code(code.getCode())
			.message(code.getMessage())
			.data(data)
			.build();
	}

	public static SuccessResponseDTO<Void> of(SuccessCode code) {
		return SuccessResponseDTO.<Void>builder()
			.code(code.getCode())
			.message(code.getMessage())
			.data(null)
			.build();
	}
}
