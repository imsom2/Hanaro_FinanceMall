package com.hana8.hanaro.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.common.exception.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(@NonNull HttpServletRequest request,
		HttpServletResponse response,
		@NonNull AuthenticationException authException) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		ErrorResponseDTO errorResponse = ErrorResponseDTO.of(ErrorCode.UNAUTHORIZED);

		response.setStatus(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
