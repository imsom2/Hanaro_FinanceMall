package com.hana8.hanaro.controller;

import com.hana8.hanaro.dto.ProductDTO;
import com.hana8.hanaro.service.ProductImageService;
import com.hana8.hanaro.service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "상품관리", description = "상품 상세에서는 ...")
public class ProductController {

	private final ProductService service;
	private final ProductImageService productImageService;

	@GetMapping
	// todo : 상품 목록과 상품 상세의 차이점 두기
	public List<ProductDTO> getProducts() {
		return service.getProducts();
	}

	@GetMapping("/{id}")
	public ProductDTO getProduct(@PathVariable Long id) {
		return service.getProduct(id);
	}

	// 상품 등록
	@PostMapping
	public ProductDTO createProduct(@Validated(ProductDTO.OnCreate.class) @RequestBody ProductDTO dto) {
		return service.createProduct(dto);
	}

	// 상품 수정
	@PutMapping("/{id}")
	public ProductDTO updateProduct(
		@PathVariable Long id,
		@Validated(ProductDTO.OnUpdate.class) @RequestBody ProductDTO dto
	) {
		dto.setId(id);
		return service.updateProduct(id, dto);
	}

	// 상품 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		service.deleteProduct(id);
		return ResponseEntity.ok().build();
	}
}
