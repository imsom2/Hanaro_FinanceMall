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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(@NonNull HttpServletRequest request, HttpServletResponse response,
		@NonNull AccessDeniedException accessDeniedException) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.FORBIDDEN.value());

		ObjectMapper objectMapper = new ObjectMapper();
		PrintWriter pw = response.getWriter();
		pw.println(objectMapper.writeValueAsString(
			Map.of("error", "ERROR_ACCESS_DENIED")));
		pw.close();

	}
}
