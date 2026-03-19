package com.hana8.hanaro.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.dto.UserAdminDTO;

import com.hana8.hanaro.dto.UserAdminSearchDTO;
import com.hana8.hanaro.dto.UserAdminSubscriptionDTO;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.service.UserAdminService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class UserAdminControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private UserAdminService userAdminService;

	private UserDetailsDTO adminUser;

	@BeforeEach
	void setUp() {
		adminUser = new UserDetailsDTO(
			1L,
			"admin@test.com",
			"1234",
			"관리자",
			Role.ROLE_ADMIN
		);
	}

	private void authenticateAdmin() {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(
				adminUser,
				null,
				adminUser.getAuthorities()
			)
		);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("회원 목록 조회")
	void getUsersTest() throws Exception {
		authenticateAdmin();

		UserAdminDTO dto = new UserAdminDTO();
		dto.setId(1L);
		dto.setName("홍길동");
		dto.setEmail("hong@test.com");

		given(userAdminService.getUsers()).willReturn(List.of(dto));

		mvc.perform(get("/api/admin/users"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("1"))
			.andExpect(jsonPath("$.data[0].name").value("홍길동"))
			.andExpect(jsonPath("$.data[0].email").value("hong@test.com"));
	}

	@Test
	@DisplayName("회원 검색")
	void searchUsersTest() throws Exception {
		authenticateAdmin();

		UserAdminSubscriptionDTO sub = new UserAdminSubscriptionDTO();
		sub.setAccountId(10L);
		sub.setProductName("적금");

		UserAdminSearchDTO dto = UserAdminSearchDTO.builder()
			.id(1L)
			.name("김하나")
			.email("hana@test.com")
			.subscriptions(List.of(sub))
			.build();

		given(userAdminService.searchUsers("hana")).willReturn(List.of(dto));

		mvc.perform(get("/api/admin/users/search")
				.param("keyword", "hana"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("1"))
			.andExpect(jsonPath("$.data[0].name").value("김하나"))
			.andExpect(jsonPath("$.data[0].email").value("hana@test.com"))
			.andExpect(jsonPath("$.data[0].subscriptions[0].accountId").value(10));
	}

	@Test
	@DisplayName("회원별 가입 상품 조회")
	void getUserSubscriptionsTest() throws Exception {
		authenticateAdmin();

		UserAdminSubscriptionDTO dto = new UserAdminSubscriptionDTO();
		dto.setAccountId(10L);
		dto.setProductName("하나로 정기적금");

		given(userAdminService.getUserSubscriptions(1L)).willReturn(List.of(dto));

		mvc.perform(get("/api/admin/users/{userId}/subscriptions", 1L))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].accountId").value("10"))
			.andExpect(jsonPath("$.data[0].productName").value("하나로 정기적금"));
	}

	@Test
	@DisplayName("계좌 만기 처리")
	void processMaturityTest() throws Exception {
		authenticateAdmin();

		mvc.perform(post("/api/admin/users/{userId}/accounts/{accountId}/maturity", 1L, 10L))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}
}
