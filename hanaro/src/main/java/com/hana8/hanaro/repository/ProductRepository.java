package com.hana8.hanaro.repository;

import com.hana8.hanaro.entity.Product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
	// 삭제되지 않은 상품 목록
	@EntityGraph(attributePaths = {"images"})
	List<Product> findByDeletedFalse();

	// 삭제되지 않은 상품 상세 조회
	Optional<Product> findByIdAndDeletedFalse(Long id);

}
