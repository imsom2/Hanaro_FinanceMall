package com.hana8.hanaro.repository;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.ProductImage;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class ProductImageRepositoryTest {

	@Autowired
	private ProductImageRepository productImageRepository;

	@Autowired
	private ProductRepository productRepository;

	private Product saveProduct(String name) {
		return productRepository.save(
			Product.builder()
				.productName(name)
				.productType(ProductType.DEPOSIT)
				.min(10000L)
				.max(10000000L)
				.period(12)
				.maturityYield(new BigDecimal("3.50"))
				.cancelYield(new BigDecimal("1.00"))
				.build()
		);
	}

	@Test
	void findByIdAndDeletedFalse_Success() {
		Product product = saveProduct("하나 정기예금");

		ProductImage image = productImageRepository.save(
			ProductImage.builder()
				.orgName("logo.png")
				.saveName("uuid_logo.png")
				.saveDir("/upload/images")
				.deleted(false)
				.product(product)
				.build()
		);

		Optional<ProductImage> result =
			productImageRepository.findByIdAndDeletedFalse(image.getId());

		assertThat(result).isPresent();
		assertThat(result.get().getOrgName()).isEqualTo("logo.png");
		assertThat(result.get().isDeleted()).isFalse();
	}

	@Test
	void findByIdAndDeletedFalse_Fail_WhenDeleted() {
		Product product = saveProduct("삭제 테스트 상품");

		ProductImage deletedImage = productImageRepository.save(
			ProductImage.builder()
				.orgName("old.png")
				.saveName("uuid_old.png")
				.saveDir("/upload/images")
				.deleted(true)
				.product(product)
				.build()
		);

		Optional<ProductImage> result =
			productImageRepository.findByIdAndDeletedFalse(deletedImage.getId());

		assertThat(result).isEmpty();
	}

}
