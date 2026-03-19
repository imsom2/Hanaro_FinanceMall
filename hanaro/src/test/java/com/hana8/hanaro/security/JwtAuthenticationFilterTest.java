package com.hana8.hanaro.security;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;

import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.security.exception.CustomJwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

	private JwtAuthenticationFilter filter;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private FilterChain filterChain;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		filter = new JwtAuthenticationFilter(jwtUtil);
	}

	@Test
	void doFilterInternal_noAuthorizationHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).contains("application/json");
		verifyNoInteractions(filterChain);
	}

	@Test
	void doFilterInternal_invalidAuthorizationHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Invalid token");

		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).contains("application/json");
		verifyNoInteractions(filterChain);
	}

	@Test
	void doFilterInternal_invalidToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer invalid");

		MockHttpServletResponse response = new MockHttpServletResponse();

		given(jwtUtil.validateToken("invalid"))
			.willThrow(new CustomJwtException(ErrorCode.JWT_INVALID));

		filter.doFilterInternal(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(401); // 또는 실제 설정값
		assertThat(response.getContentType()).contains("application/json");
		verifyNoInteractions(filterChain);
	}

	@Test
	void doFilterInternal_validToken_setsAuthentication() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer validToken");

		MockHttpServletResponse response = new MockHttpServletResponse();

		Map<String, Object> claims = Map.of(
			"id", 1L,
			"email", "test@test.com",
			"name", "테스트",
			"role", "ROLE_USER"
		);

		given(jwtUtil.validateToken("validToken"))
			.willReturn(claims);

		filter.doFilterInternal(request, response, filterChain);

		assert SecurityContextHolder.getContext().getAuthentication() != null;

		verify(filterChain).doFilter(request, response);
	}
}
