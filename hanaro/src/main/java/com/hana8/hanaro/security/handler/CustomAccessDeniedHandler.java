package com.hana8.hanaro.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.common.exception.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(@NonNull HttpServletRequest request, HttpServletResponse response,
		@NonNull AccessDeniedException accessDeniedException) throws IOException {
		ErrorResponseDTO errorResponse = ErrorResponseDTO.of(ErrorCode.ACCESS_DENIED);
		ObjectMapper objectMapper = new ObjectMapper();

		response.setStatus(ErrorCode.ACCESS_DENIED.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
