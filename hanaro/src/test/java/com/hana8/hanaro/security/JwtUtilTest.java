package com.hana8.hanaro.security;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.security.exception.CustomJwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class JwtUtilTest {

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil("test-secret-key-for-jwt-signing-123456");
	}

	@Test
	void generateToken_and_validateToken_success() {
		Map<String, Object> claims = Map.of(
			"id", 1L,
			"email", "test@test.com",
			"name", "테스트",
			"role", "ROLE_USER"
		);

		String token = jwtUtil.generateToken(claims, 10);
		Map<String, Object> result = jwtUtil.validateToken(token);

		assertThat(result.get("email")).isEqualTo("test@test.com");
		assertThat(result.get("name")).isEqualTo("테스트");
		assertThat(result.get("role")).isEqualTo("ROLE_USER");
	}

	@Test
	void validateToken_fail_whenInvalidToken() {
		assertThatThrownBy(() -> jwtUtil.validateToken("invalid-token"))
			.isInstanceOf(CustomJwtException.class)
			.satisfies(e -> {
				CustomJwtException ex = (CustomJwtException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.JWT_INVALID);
			});
	}

	@Test
	void validateToken_fail_whenExpiredToken() {
		Map<String, Object> claims = Map.of(
			"id", 1L,
			"email", "expired@test.com"
		);

		String expiredToken = jwtUtil.generateToken(claims, -1);

		assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken))
			.isInstanceOf(CustomJwtException.class)
			.satisfies(e -> {
				CustomJwtException ex = (CustomJwtException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.JWT_EXPIRED);
			});
	}

	@Test
	void authenticationToClaims_success() {
		UserDetailsDTO userDetails = new UserDetailsDTO(
			1L,
			"user@test.com",
			"1234",
			"유저",
			Role.ROLE_USER
		);
		userDetails.setAccountId(100L);

		Authentication authentication =
			new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				userDetails.getAuthorities()
			);

		Map<String, Object> result = jwtUtil.authenticationToClaims(authentication);

		assertThat(result.get("id")).isEqualTo(1L);
		assertThat(result.get("email")).isEqualTo("user@test.com");
		assertThat(result.get("name")).isEqualTo("유저");
		assertThat(result.get("role")).isEqualTo("ROLE_USER");
		assertThat(result.get("accountId")).isEqualTo("100");
		assertThat(result).containsKeys("accessToken", "refreshToken");
	}

	@Test
	void authenticationToClaims_fail_whenPrincipalIsNull() {
		Authentication authentication =
			new UsernamePasswordAuthenticationToken(null, null);

		assertThatThrownBy(() -> jwtUtil.authenticationToClaims(authentication))
			.isInstanceOf(CustomJwtException.class)
			.satisfies(e -> {
				CustomJwtException ex = (CustomJwtException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.JWT_INVALID);
			});
	}
}
