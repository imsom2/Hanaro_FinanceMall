package com.hana8.hanaro.repository;

import com.hana8.hanaro.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	int countByProductId(Long productId);
}
