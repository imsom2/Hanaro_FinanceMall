package com.hana8.hanaro.security.handler;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import com.hana8.hanaro.common.exception.ErrorCode;

class CustomAuthenticationEntryPointTest {

	private final CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();

	@Test
	void commence_returns401JsonResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		AuthenticationException exception = new AuthenticationException("인증 실패") {};

		entryPoint.commence(request, response, exception);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).contains("application/json");
		assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

		String body = response.getContentAsString(StandardCharsets.UTF_8);
		assertThat(body).contains(ErrorCode.UNAUTHORIZED.getCode());
		assertThat(body).contains(ErrorCode.UNAUTHORIZED.getMessage());
		assertThat(body).contains("false");
	}
}
