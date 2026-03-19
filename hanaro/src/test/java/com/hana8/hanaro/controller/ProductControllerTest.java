package com.hana8.hanaro.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.dto.SubscriptionRequestDTO;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.ProductRepository;
import com.hana8.hanaro.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	private UserDetailsDTO testUser;
	private Long savedProductId;
	private Long depositProductId;

	@BeforeEach
	void setUp() {
		User savedUser = userRepository.save(
			User.builder()
				.email("test@test.com")
				.passwd("1234")
				.name("테스트유저")
				.role(Role.ROLE_USER)
				.build()
		);

		Product savedProduct = productRepository.save(
			Product.builder()
				.productName("하나로 정기적금")
				.productType(ProductType.SAVINGS)
				.min(10000L)
				.max(1000000L)
				.period(12)
				.maturityYield(new BigDecimal("3.50"))
				.cancelYield(new BigDecimal("1.00"))
				.description("테스트 상품")
				.deleted(false)
				.build()
		);

		savedProductId = savedProduct.getId();

		Product depositProduct = productRepository.save(
			Product.builder()
				.productName("하나로 정기예금")
				.productType(ProductType.DEPOSIT)
				.min(10000L)
				.max(10000000L)
				.period(12)
				.maturityYield(new BigDecimal("3.50"))
				.cancelYield(new BigDecimal("1.00"))
				.description("테스트 예금 상품")
				.deleted(false)
				.build()
		);
		depositProductId = depositProduct.getId();

		// DEPOSIT 가입 테스트용 기본 계좌 생성
		accountRepository.save(
			Account.builder()
				.accountNum(AccountUtil.encrypt("99900000001"))
				.accountType(AccountType.BASIC)
				.balance(5000000L)
				.user(savedUser)
				.build()
		);

		testUser = new UserDetailsDTO(
			savedUser.getId(),
			savedUser.getEmail(),
			savedUser.getPasswd(),
			savedUser.getName(),
			savedUser.getRole()
		);
	}

	private void authenticate() {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(
				testUser,
				null,
				testUser.getAuthorities()
			)
		);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("상품 목록 조회")
	void getProductsTest() throws Exception {
		mvc.perform(get("/api/products"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@DisplayName("상품 상세 조회")
	void getProductTest() throws Exception {
		mvc.perform(get("/api/products/" + savedProductId))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.id").value(savedProductId))
			.andExpect(jsonPath("$.data.productName").value("하나로 정기적금"));
	}

	@Test
	@DisplayName("상품 가입 - 월별 적금")
	void subscribeTest_monthlySavings() throws Exception {
		authenticate();

		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentAmount(10000L);
		dto.setPaymentCycle(PaymentCycle.MONTHLY);
		dto.setPaymentDay(15);
		dto.setWishAccountNum("12345678901");

		mvc.perform(post("/api/products/" + savedProductId + "/subscribe")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("상품 가입 - 주별 적금")
	void subscribeTest_weeklySavings() throws Exception {
		authenticate();

		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentAmount(10000L);
		dto.setPaymentCycle(PaymentCycle.WEEKLY);
		dto.setPaymentDayOfWeek(DayOfWeek.MONDAY);
		dto.setWishAccountNum("12300000001");

		mvc.perform(post("/api/products/" + savedProductId + "/subscribe")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("상품 가입 - 정기예금")
	void subscribeTest_deposit() throws Exception {
		authenticate();

		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentAmount(100000L);
		dto.setWishAccountNum("12300000002");

		mvc.perform(post("/api/products/" + depositProductId + "/subscribe")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true));
	}
}
