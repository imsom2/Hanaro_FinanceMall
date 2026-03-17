package com.hana8.hanaro.service;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.auth.LoginRequest;
import com.hana8.hanaro.security.JwtUtil;
import com.hana8.hanaro.security.exception.CustomJwtException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public Map<String, Object> signIn(LoginRequest loginRequest) {
		try {
			Authentication authenticate = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginRequest.email(),
					loginRequest.passwd()
				)
			);

			return jwtUtil.authenticationToClaims(authenticate);
		} catch (AuthenticationException e) {
			throw new BusinessException(ErrorCode.LOGIN_FAILED);
		}
	}

	public Map<String, Object> refresh(String authHeader, String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BusinessException(ErrorCode.JWT_INVALID, "리프레시 토큰이 없습니다.");
		}

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new BusinessException(ErrorCode.JWT_INVALID, "Authorization 헤더가 올바르지 않습니다.");
		}

		String accessToken = authHeader.substring(7);

		if (!didExpireToken(accessToken)) {
			return Map.of(
				"accessToken", accessToken,
				"refreshToken", refreshToken
			);
		}

		Map<String, Object> claim = jwtUtil.validateToken(refreshToken);

		String newAccessToken = jwtUtil.generateToken(claim, 10);
		String newRefreshToken = isSomeLeftTime((long) claim.get("exp"))
			? jwtUtil.generateToken(claim, 60 * 24)
			: refreshToken;

		return Map.of(
			"accessToken", newAccessToken,
			"refreshToken", newRefreshToken
		);
	}

	private boolean isSomeLeftTime(long exp) {
		long nowSec = System.currentTimeMillis() / 1000;
		return (exp - nowSec) < 60 * 60;
	}

	private boolean didExpireToken(String accessToken) {
		try {
			jwtUtil.validateToken(accessToken);
		} catch (CustomJwtException e) {
			return true;
		}
		return false;
	}
}
