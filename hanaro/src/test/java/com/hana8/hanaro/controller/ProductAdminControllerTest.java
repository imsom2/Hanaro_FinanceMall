package com.hana8.hanaro.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

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

import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.dto.ProductRequestDTO;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.repository.ProductRepository;
import com.hana8.hanaro.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductAdminControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	private UserDetailsDTO adminUser;
	private Long savedProductId;

	@BeforeEach
	void setUp() {
		User savedAdmin = userRepository.save(
			User.builder()
				.email("admin@test.com")
				.passwd("1234")
				.name("관리자")
				.role(Role.ROLE_ADMIN)
				.build()
		);

		adminUser = new UserDetailsDTO(
			savedAdmin.getId(),
			savedAdmin.getEmail(),
			savedAdmin.getPasswd(),
			savedAdmin.getName(),
			savedAdmin.getRole()
		);

		Product savedProduct = productRepository.save(
			Product.builder()
				.productName("기존 적금 상품")
				.productType(ProductType.SAVINGS)
				.min(10000L)
				.max(1000000L)
				.period(12)
				.maturityYield(new BigDecimal("3.50"))
				.cancelYield(new BigDecimal("1.00"))
				.description("기존 상품 설명")
				.deleted(false)
				.build()
		);

		savedProductId = savedProduct.getId();
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
	@DisplayName("관리자 상품 등록")
	void createProductTest() throws Exception {
		authenticateAdmin();

		ProductRequestDTO request = new ProductRequestDTO();
		request.setProductName("하나로 자유적금");
		request.setProductType(ProductType.SAVINGS);
		request.setMin(10000L);
		request.setMax(5000000L);
		request.setPeriod(12);
		request.setMaturityYield(new BigDecimal("3.80"));
		request.setCancelYield(new BigDecimal("1.20"));
		request.setDescription("테스트용 신규 상품");

		mvc.perform(post("/api/admin/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.productName").value("하나로 자유적금"))
			.andExpect(jsonPath("$.data.productType").value("SAVINGS"))
			.andExpect(jsonPath("$.data.min").value(10000))
			.andExpect(jsonPath("$.data.max").value(5000000));
	}

	@Test
	@DisplayName("관리자 상품 수정")
	void updateProductTest() throws Exception {
		authenticateAdmin();

		ProductRequestDTO request = new ProductRequestDTO();
		request.setProductName("수정된 적금 상품");
		request.setProductType(ProductType.SAVINGS);
		request.setMin(20000L);
		request.setMax(3000000L);
		request.setPeriod(24);
		request.setMaturityYield(new BigDecimal("4.10"));
		request.setCancelYield(new BigDecimal("1.50"));
		request.setDescription("수정된 상품 설명");

		mvc.perform(put("/api/admin/products/{productId}", savedProductId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.id").value(savedProductId))
			.andExpect(jsonPath("$.data.productName").value("수정된 적금 상품"))
			.andExpect(jsonPath("$.data.period").value(24))
			.andExpect(jsonPath("$.data.min").value(20000));
	}

	@Test
	@DisplayName("관리자 상품 삭제")
	void deleteProductTest() throws Exception {
		authenticateAdmin();

		mvc.perform(delete("/api/admin/products/{productId}", savedProductId))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true));

		Product deletedProduct = productRepository.findById(savedProductId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertTrue(deletedProduct.isDeleted());
	}
}
