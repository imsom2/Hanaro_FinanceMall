package com.hana8.hanaro.repository;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.ProductImage;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductImageRepository productImageRepository;

	@Autowired
	private EntityManager entityManager;

	private Product createProduct(String name, boolean deleted) {
		return productRepository.save(
			Product.builder()
				.productName(name)
				.productType(ProductType.SAVINGS)
				.min(1000L)
				.max(1000000L)
				.period(12)
				.maturityYield(new BigDecimal("4.0"))
				.cancelYield(new BigDecimal("1.5"))
				.deleted(deleted)
				.build()
		);
	}

	@Test
	void findByDeletedFalse_WithImages() {
		Product product = createProduct("하나 적금", false);

		productImageRepository.save(
			ProductImage.builder()
				.orgName("thumb.png")
				.saveName("uuid_thumb.png")
				.saveDir("/dir")
				.product(product)
				.build()
		);

		createProduct("삭제된 상품", true);

		entityManager.flush();
		entityManager.clear();

		List<Product> result = productRepository.findByDeletedFalse();

		assertThat(result)
			.extracting(Product::getProductName)
			.contains("하나 적금");

		Product foundProduct = result.stream()
			.filter(p -> p.getProductName().equals("하나 적금"))
			.findFirst()
			.orElseThrow();

		assertThat(foundProduct.getImages()).isNotEmpty();

		assertThat(foundProduct.toString()).isNotBlank();
	}

	@Test
	void findByIdAndDeletedFalse() {
		Product product = createProduct("하나 예금", false);

		Optional<Product> found =
			productRepository.findByIdAndDeletedFalse(product.getId());

		Optional<Product> notFound =
			productRepository.findByIdAndDeletedFalse(999L);

		assertThat(found).isPresent();
		assertThat(found.get().getProductName()).isEqualTo("하나 예금");
		assertThat(notFound).isEmpty();
	}

}
