package com.hana8.hanaro.security.handler;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.hana8.hanaro.common.exception.ErrorCode;

class CustomAccessDeniedHandlerTest {

	private final CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();

	@Test
	void handle_returns403JsonResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		AccessDeniedException exception = new AccessDeniedException("권한 없음");

		handler.handle(request, response, exception);

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentType()).contains("application/json");
		assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

		String body = response.getContentAsString(StandardCharsets.UTF_8);
		assertThat(body).contains(ErrorCode.ACCESS_DENIED.getCode());
		assertThat(body).contains(ErrorCode.ACCESS_DENIED.getMessage());
		assertThat(body).contains("false");
	}
}
