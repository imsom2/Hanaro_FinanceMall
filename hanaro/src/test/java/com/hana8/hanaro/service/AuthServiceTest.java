package com.hana8.hanaro.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.auth.LoginRequest;
import com.hana8.hanaro.security.JwtUtil;
import com.hana8.hanaro.security.exception.CustomJwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtil jwtUtil;

	private LoginRequest loginRequest;

	@BeforeEach
	void setUp() {
		loginRequest = new LoginRequest("test@test.com", "1234");
	}

	@Test
	void signIn_success() {
		Authentication authentication =
			new UsernamePasswordAuthenticationToken("test@test.com", "1234");

		Map<String, Object> tokenMap = Map.of(
			"accessToken", "access-token",
			"refreshToken", "refresh-token"
		);

		given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.willReturn(authentication);
		given(jwtUtil.authenticationToClaims(authentication)).willReturn(tokenMap);

		Map<String, Object> result = authService.signIn(loginRequest);

		assertThat(result.get("accessToken")).isEqualTo("access-token");
		assertThat(result.get("refreshToken")).isEqualTo("refresh-token");
	}

	@Test
	void signIn_fail_whenAuthenticationException() {
		given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.willThrow(new BadCredentialsException("bad credentials"));

		assertThatThrownBy(() -> authService.signIn(loginRequest))
			.isInstanceOf(BusinessException.class)
			.hasMessage(ErrorCode.LOGIN_FAILED.getMessage());
	}

	@Test
	void refresh_fail_whenRefreshTokenIsBlank() {
		assertThatThrownBy(() -> authService.refresh("Bearer access-token", ""))
			.isInstanceOf(BusinessException.class)
			.hasMessage("리프레시 토큰이 없습니다.");
	}

	@Test
	void refresh_fail_whenAuthorizationHeaderIsInvalid() {
		assertThatThrownBy(() -> authService.refresh("invalid-header", "refresh-token"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("Authorization 헤더가 올바르지 않습니다.");
	}

	@Test
	void refresh_returnOriginalTokens_whenAccessTokenNotExpired() {
		String accessToken = "valid-access-token";
		String refreshToken = "valid-refresh-token";

		given(jwtUtil.validateToken(accessToken)).willReturn(Map.of("sub", "test"));

		Map<String, Object> result = authService.refresh("Bearer " + accessToken, refreshToken);

		assertThat(result.get("accessToken")).isEqualTo(accessToken);
		assertThat(result.get("refreshToken")).isEqualTo(refreshToken);
	}

	@Test
	void refresh_generateNewAccessAndRefresh_whenAccessExpired_andRefreshAlmostExpired() {
		String accessToken = "expired-access-token";
		String refreshToken = "refresh-token";

		long nowSec = System.currentTimeMillis() / 1000;
		Map<String, Object> refreshClaims = Map.of(
			"sub", "test@test.com",
			"exp", nowSec + 1800 // 30분 남음 -> 1시간 미만
		);

		given(jwtUtil.validateToken(accessToken))
			.willThrow(new CustomJwtException(ErrorCode.JWT_EXPIRED));
		given(jwtUtil.validateToken(refreshToken)).willReturn(refreshClaims);
		given(jwtUtil.generateToken(refreshClaims, 10)).willReturn("new-access-token");
		given(jwtUtil.generateToken(refreshClaims, 60 * 24)).willReturn("new-refresh-token");

		Map<String, Object> result = authService.refresh("Bearer " + accessToken, refreshToken);

		assertThat(result.get("accessToken")).isEqualTo("new-access-token");
		assertThat(result.get("refreshToken")).isEqualTo("new-refresh-token");
	}

	@Test
	void refresh_generateOnlyNewAccess_whenAccessExpired_andRefreshHasEnoughTime() {
		String accessToken = "expired-access-token";
		String refreshToken = "refresh-token";

		long nowSec = System.currentTimeMillis() / 1000;
		Map<String, Object> refreshClaims = Map.of(
			"sub", "test@test.com",
			"exp", nowSec + 7200 // 2시간 남음 -> 기존 refresh 유지
		);

		given(jwtUtil.validateToken(accessToken))
			.willThrow(new CustomJwtException(ErrorCode.JWT_EXPIRED));
		given(jwtUtil.validateToken(refreshToken)).willReturn(refreshClaims);
		given(jwtUtil.generateToken(refreshClaims, 10)).willReturn("new-access-token");

		Map<String, Object> result = authService.refresh("Bearer " + accessToken, refreshToken);

		assertThat(result.get("accessToken")).isEqualTo("new-access-token");
		assertThat(result.get("refreshToken")).isEqualTo(refreshToken);
	}
}
