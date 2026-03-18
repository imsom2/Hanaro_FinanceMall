package com.hana8.hanaro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.common.exception.ErrorResponseDTO;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.security.exception.CustomJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String[] EXCLUDE_PATTERNS = {
		"/api/auth/signin",
		"/api/auth/signup",
		"/api/auth/refresh",
		"/favicon.ico",
		"/actuator/**",
		"/swagger-ui/**",
		"/v3/api-docs/**",
	};

	private final JwtUtil jwtUtil;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();

		return Arrays.stream(EXCLUDE_PATTERNS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			sendError(response, ErrorCode.UNAUTHORIZED);
			return;
		}

		try {
			Map<String, Object> claims = jwtUtil.validateToken(authHeader.substring(7));

			Object idObj = claims.get("id");
			Object emailObj = claims.get("email");
			Object nameObj = claims.get("name");
			Object roleObj = claims.get("role");

			if (idObj == null || emailObj == null || nameObj == null || roleObj == null) {
				sendError(response, ErrorCode.JWT_INVALID);
				return;
			}

			UserDetailsDTO dto = new UserDetailsDTO(
				Long.valueOf(idObj.toString()),
				(String) emailObj,
				"",
				(String) nameObj,
				Role.valueOf((String) roleObj)
			);

			SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(dto, null, dto.getAuthorities())
			);
		} catch (CustomJwtException e) {
			sendError(response, e.getErrorCode());
			return;
		} catch (IllegalArgumentException | ClassCastException e) {
			sendError(response, ErrorCode.JWT_INVALID);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void sendError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		ErrorResponseDTO errorResponse = ErrorResponseDTO.of(errorCode);
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
