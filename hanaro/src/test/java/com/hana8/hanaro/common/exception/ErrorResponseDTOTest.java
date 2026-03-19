package com.hana8.hanaro.common.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorResponseDTOTest {

	@Test
	void of_withErrorCode_setsAllFields() {
		ErrorResponseDTO dto = ErrorResponseDTO.of(ErrorCode.INVALID_INPUT);

		assertThat(dto.isSuccess()).isFalse();
		assertThat(dto.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(dto.getCode()).isEqualTo("COMMON_001");
		assertThat(dto.getMessage()).isEqualTo("잘못된 입력입니다.");
	}

	@Test
	void of_withErrorCodeAndCustomMessage_overridesMessage() {
		ErrorResponseDTO dto = ErrorResponseDTO.of(ErrorCode.INVALID_INPUT, "커스텀 메시지");

		assertThat(dto.isSuccess()).isFalse();
		assertThat(dto.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(dto.getCode()).isEqualTo("COMMON_001");
		assertThat(dto.getMessage()).isEqualTo("커스텀 메시지");
	}

	@Test
	void of_notFoundErrorCode_returns404Status() {
		ErrorResponseDTO dto = ErrorResponseDTO.of(ErrorCode.USER_NOT_FOUND);

		assertThat(dto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(dto.getCode()).isEqualTo("USER_001");
		assertThat(dto.isSuccess()).isFalse();
	}

	@Test
	void of_unauthorizedErrorCode_returns401Status() {
		ErrorResponseDTO dto = ErrorResponseDTO.of(ErrorCode.JWT_EXPIRED);

		assertThat(dto.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
		assertThat(dto.getCode()).isEqualTo("AUTH_003");
	}

	@Test
	void of_conflictErrorCode_returns409Status() {
		ErrorResponseDTO dto = ErrorResponseDTO.of(ErrorCode.USER_EMAIL_DUPLICATE);

		assertThat(dto.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
		assertThat(dto.getCode()).isEqualTo("USER_002");
	}
}
