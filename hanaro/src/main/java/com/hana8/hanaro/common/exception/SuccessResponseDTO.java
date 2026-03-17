package com.hana8.hanaro.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SuccessResponseDTO<T> {
	private final boolean isSuccess = true;
	private final String code;
	private final String message;
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
			.data(null) // Void이므로 null 세팅
			.build();
	}
}
