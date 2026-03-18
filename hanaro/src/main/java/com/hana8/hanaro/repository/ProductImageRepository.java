package com.hana8.hanaro.repository;

import java.util.List;
import java.util.Optional;

import com.hana8.hanaro.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	Optional<ProductImage> findByIdAndDeletedFalse(Long id);

	List<ProductImage> findByProductIdAndDeletedFalse(Long productId);
}
