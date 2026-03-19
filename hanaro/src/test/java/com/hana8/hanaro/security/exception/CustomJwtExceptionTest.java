package com.hana8.hanaro.security.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.hana8.hanaro.common.exception.ErrorCode;

class CustomJwtExceptionTest {

	@Test
	void constructor_withErrorCodeAndMessage() {
		CustomJwtException exception =
			new CustomJwtException(ErrorCode.JWT_INVALID, "테스트용 메시지");

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.JWT_INVALID);
		assertThat(exception.getMessage()).isEqualTo("테스트용 메시지");
	}
}
