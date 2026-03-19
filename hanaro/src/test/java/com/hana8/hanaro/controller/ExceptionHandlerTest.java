package com.hana8.hanaro.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.validation.ConstraintViolationException;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.service.ProductService;
import com.hana8.hanaro.service.SubscriptionService;
import com.hana8.hanaro.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ExceptionHandlerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProductService productService;

	@MockitoBean
	private SubscriptionService subscriptionService;

	@MockitoBean
	private UserService userService;

	// ── ControllerExceptionHandler ───────────────────────────────────────

	@Test
	void handleMethodArgumentNotValid() throws Exception {
		// @Valid 검증 실패 → MethodArgumentNotValidException
		mvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_001"));
	}

	@Test
	void handleConstraintViolation() throws Exception {
		given(productService.getProducts()).willThrow(new ConstraintViolationException(Set.of()));

		mvc.perform(get("/api/products"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_001"));
	}

	@Test
	void handleNoHandlerFound() throws Exception {
		// 존재하지 않는 엔드포인트 → NoHandlerFoundException
		mvc.perform(get("/api/this-endpoint-does-not-exist-xyz"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("COMMON_006"));
	}

	@Test
	void handleAccessDenied() throws Exception {
		given(productService.getProducts()).willThrow(new AccessDeniedException("forbidden"));

		mvc.perform(get("/api/products"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("AUTH_002"));
	}

	@Test
	void handleAuthorizationDenied() throws Exception {
		given(productService.getProducts()).willThrow(
			new AuthorizationDeniedException("denied", new AuthorizationDecision(false)));

		mvc.perform(get("/api/products"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("AUTH_002"));
	}

	// ── GlobalExceptionHandler ───────────────────────────────────────────

	@Test
	void handleBusinessException() throws Exception {
		given(productService.getProducts()).willThrow(
			new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

		mvc.perform(get("/api/products"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PRODUCT_001"));
	}

	@Test
	void handleDataIntegrityViolation_emailDuplicate() throws Exception {
		RuntimeException cause = new RuntimeException("CONSTRAINT uniq_User_email");
		DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", cause);
		given(userService.signUp(any())).willThrow(ex);

		mvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@example.com\",\"passwd\":\"Test1234!\",\"name\":\"테스터\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("USER_002"));
	}

	@Test
	void handleDataIntegrityViolation_accountNumDuplicate() throws Exception {
		RuntimeException cause = new RuntimeException("CONSTRAINT uniq_Account_accountNum");
		DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", cause);
		given(userService.signUp(any())).willThrow(ex);

		mvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@example.com\",\"passwd\":\"Test1234!\",\"name\":\"테스터\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("ACCOUNT_002"));
	}

	@Test
	void handleDataIntegrityViolation_other() throws Exception {
		RuntimeException cause = new RuntimeException("some other constraint");
		DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", cause);
		given(userService.signUp(any())).willThrow(ex);

		mvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@example.com\",\"passwd\":\"Test1234!\",\"name\":\"테스터\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("COMMON_005"));
	}

	@Test
	void handleException_fallback() throws Exception {
		given(productService.getProducts()).willThrow(new RuntimeException("unexpected error"));

		mvc.perform(get("/api/products"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value("COMMON_004"));
	}
}
