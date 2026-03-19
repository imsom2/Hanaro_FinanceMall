package com.hana8.hanaro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hana8.hanaro.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	@EntityGraph(attributePaths = {"images"})
	List<Product> findByDeletedFalse();

	Optional<Product> findByIdAndDeletedFalse(Long id);
}
