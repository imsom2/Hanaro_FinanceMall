package com.hana8.hanaro.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.dto.auth.LoginRequest;
import com.hana8.hanaro.dto.auth.RefreshRequest;
import com.hana8.hanaro.dto.auth.SignUpDTO;
import com.hana8.hanaro.dto.auth.SignUpRequestDTO;
import com.hana8.hanaro.service.AuthService;
import com.hana8.hanaro.service.UserService;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private AuthService authService;

	@Test
	@DisplayName("회원가입")
	void signUpTest() throws Exception {
		SignUpRequestDTO request = new SignUpRequestDTO();
		request.setEmail("test@test.com");
		request.setPasswd("Test1234!");
		request.setName("테스트유저");
		request.setAccountNum("12345678901");

		SignUpDTO response = SignUpDTO.builder()
			.id(1L)
			.email("test@test.com")
			.name("테스트유저")
			.accountNum("123-****-8901")
			.build();

		given(userService.signUp(request)).willReturn(response);

		mvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value(SuccessCode.SIGNUP_SUCCESS.getCode()))
			.andExpect(jsonPath("$.data.email").value("test@test.com"))
			.andExpect(jsonPath("$.data.name").value("테스트유저"));
	}

	@Test
	@DisplayName("로그인")
	void signInTest() throws Exception {
		LoginRequest request = new LoginRequest("test@test.com", "Test1234!");

		Map<String, Object> tokenMap = Map.of(
			"accessToken", "access-token-sample",
			"refreshToken", "refresh-token-sample"
		);

		given(authService.signIn(request)).willReturn(tokenMap);

		mvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value(SuccessCode.SIGNIN_SUCCESS.getCode()))
			.andExpect(jsonPath("$.data.accessToken").value("access-token-sample"))
			.andExpect(jsonPath("$.data.refreshToken").value("refresh-token-sample"));
	}

	@Test
	@DisplayName("토큰 재발급")
	void refreshTest() throws Exception {
		RefreshRequest request = new RefreshRequest("refresh-token-sample");

		Map<String, Object> tokenMap = Map.of(
			"accessToken", "new-access-token",
			"refreshToken", "new-refresh-token"
		);

		given(authService.refresh("Bearer old-access-token", "refresh-token-sample"))
			.willReturn(tokenMap);

		mvc.perform(post("/api/auth/refresh")
				.header("Authorization", "Bearer old-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value(SuccessCode.TOKEN_REFRESH_SUCCESS.getCode()))
			.andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
			.andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
	}
}
