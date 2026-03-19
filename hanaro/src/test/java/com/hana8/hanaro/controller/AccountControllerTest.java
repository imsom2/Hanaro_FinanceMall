package com.hana8.hanaro.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.ProductRepository;
import com.hana8.hanaro.repository.SubscriptionRepository;
import com.hana8.hanaro.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AccountControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	private UserDetailsDTO testUser;
	private Long savedAccountId;

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

		testUser = new UserDetailsDTO(
			savedUser.getId(),
			savedUser.getEmail(),
			savedUser.getPasswd(),
			savedUser.getName(),
			savedUser.getRole()
		);

		Account savedAccount = accountRepository.save(
			Account.builder()
				.accountNum(AccountUtil.encrypt("12345678901"))
				.accountType(AccountType.BASIC)
				.balance(10000L)
				.user(savedUser)
				.build()
		);

		savedAccountId = savedAccount.getId();
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
	@DisplayName("전체 계좌 조회")
	void getAllAccountsTest() throws Exception {
		authenticate();

		mvc.perform(get("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("계좌 상세 조회")
	void getAccountDetailTest() throws Exception {
		authenticate();

		mvc.perform(get("/api/accounts/" + savedAccountId))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("납입 이체")
	void transferTest() throws Exception {
		authenticate();

		User savedUser = userRepository.findById(testUser.getId()).orElseThrow();

		Product product = productRepository.save(
			Product.builder()
				.productName("하나로 정기적금")
				.productType(ProductType.SAVINGS)
				.min(10000L)
				.max(1000000L)
				.period(12)
				.maturityYield(new BigDecimal("3.5"))
				.cancelYield(new BigDecimal("1.0"))
				.description("테스트 상품")
				.deleted(false)
				.build()
		);

		Account subAccount = accountRepository.save(
			Account.builder()
				.accountNum(AccountUtil.encrypt("98765432109"))
				.accountType(AccountType.SAVINGS)
				.balance(0L)
				.user(savedUser)
				.build()
		);

		subscriptionRepository.save(
			Subscription.builder()
				.user(savedUser)
				.product(product)
				.account(subAccount)
				.status(AccountStatus.ACTIVE)
				.paymentCycle(PaymentCycle.MONTHLY)
				.paymentAmount(10000L)
				.paymentDay(LocalDate.now().getDayOfMonth())
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);

		mvc.perform(post("/api/accounts/{accoutId}/transfer", subAccount.getId()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("입출금 계좌 충전")
	void depositTest() throws Exception {
		authenticate();

		String requestBody = """
	{
	  "amount": 5000
	}
	""";

		mvc.perform(post("/api/accounts/{accountId}/deposit", savedAccountId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("상품 중도 해지")
	void cancelTest() throws Exception {
		authenticate();

		User savedUser = userRepository.findById(testUser.getId()).orElseThrow();

		Product product = productRepository.save(
			Product.builder()
				.productName("하나로 정기적금")
				.productType(ProductType.SAVINGS)
				.min(10000L)
				.max(1000000L)
				.period(12)
				.maturityYield(new BigDecimal("3.5"))
				.cancelYield(new BigDecimal("1.0"))
				.description("테스트 상품")
				.deleted(false)
				.build()
		);

		Account subAccount = accountRepository.save(
			Account.builder()
				.accountNum(AccountUtil.encrypt("98765432109"))
				.accountType(AccountType.SAVINGS)
				.balance(10000L)
				.user(savedUser)
				.build()
		);

		subscriptionRepository.save(
			Subscription.builder()
				.user(savedUser)
				.product(product)
				.account(subAccount)
				.status(AccountStatus.ACTIVE)
				.paymentCycle(PaymentCycle.MONTHLY)
				.paymentAmount(10000L)
				.paymentDay(LocalDate.now().getDayOfMonth())
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);

		mvc.perform(post("/api/accounts/{accountId}/cancel", subAccount.getId()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

}
