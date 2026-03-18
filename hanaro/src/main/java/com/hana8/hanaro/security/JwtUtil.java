package com.hana8.hanaro.security;

import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.security.exception.CustomJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

	private final SecretKey secretKey;

	public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
		secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public Map<String, Object> validateToken(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			log.warn("JWT Token Expired: {}", e.getMessage());
			throw new CustomJwtException(ErrorCode.JWT_EXPIRED);
		} catch (JwtException e) {
			log.error("Invalid JWT Token: {}", e.getMessage());
			throw new CustomJwtException(ErrorCode.JWT_INVALID);
		} catch (Exception e) {
			log.error("Unexpected JWT Error: ", e);
			throw new CustomJwtException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 처리 중 서버 오류가 발생했습니다: " + e.getMessage());		}
	}

	public Map<String, Object> authenticationToClaims(Authentication authentication) {
		UserDetailsDTO dto = (UserDetailsDTO) authentication.getPrincipal();

		if (dto == null) {
			throw new CustomJwtException(ErrorCode.JWT_INVALID);
		}
		Map<String, Object> claims = dto.getClaims();
		Map<String, Object> tokenClaims = new java.util.HashMap<>(claims);
		tokenClaims.put("accessToken", generateToken(claims, 10));
		tokenClaims.put("refreshToken", generateToken(claims, 60 * 24));

		return tokenClaims;
	}

	public String generateToken(Map<String, Object> valueMap, int min) {
		return Jwts.builder()
			.header().add("typ", "JWT").and()
			.claims().add(valueMap).and()
			.issuedAt(Date.from(ZonedDateTime.now().toInstant()))
			.expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
			.signWith(secretKey)
			.compact();
	}
}
