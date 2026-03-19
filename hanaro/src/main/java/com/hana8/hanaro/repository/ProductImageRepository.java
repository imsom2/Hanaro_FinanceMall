package com.hana8.hanaro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hana8.hanaro.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	Optional<ProductImage> findByIdAndDeletedFalse(Long id);
}
