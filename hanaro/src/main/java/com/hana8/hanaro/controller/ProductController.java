package com.hana8.hanaro.controller;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.ProductDTO;
import com.hana8.hanaro.dto.ProductListDTO;
import com.hana8.hanaro.dto.ProductRequestDTO;
import com.hana8.hanaro.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "상품", description = "예적금 상품에 관련한 API입니다")
public class ProductController {

	private final ProductService service;

	@Operation(summary = "상품 목록 조회", description = "삭제되지 않은 예적금 상품 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
	@GetMapping
	public SuccessResponseDTO<List<ProductListDTO>> getProducts() {
		List<ProductListDTO> data = service.getProducts();
		// 문자열 대신 SuccessCode 사용으로 통일
		return SuccessResponseDTO.of(SuccessCode._OK, data);
	}

	@Operation(summary = "상품 상세 조회", description = "상품 ID로 특정 상품의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 상세 조회 성공"),
		@ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
	})
	@GetMapping("/{id}")
	public SuccessResponseDTO<ProductDTO> getProduct(@PathVariable Long id) {
		ProductDTO data = service.getProduct(id);
		return SuccessResponseDTO.of(SuccessCode._OK, data);
	}

	@Operation(summary = "상품 등록", description = "새 예적금 상품을 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 등록 성공"),
		@ApiResponse(responseCode = "400", description = "입력값 오류"),
		@ApiResponse(responseCode = "409", description = "중복 데이터")
	})
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponseDTO<ProductDTO> createProduct(@Validated(ProductRequestDTO.OnCreate.class) @RequestBody ProductDTO dto) {
		ProductDTO data = service.createProduct(dto);
		return SuccessResponseDTO.of(SuccessCode.PRODUCT_CREATED, data);
	}

	@Operation(summary = "상품 수정", description = "기존 예적금 상품 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 수정 성공"),
		@ApiResponse(responseCode = "400", description = "입력값 오류"),
		@ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
	})
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponseDTO<ProductDTO> updateProduct(
		@PathVariable Long id,
		@Validated(ProductRequestDTO.OnUpdate.class) @RequestBody ProductDTO dto
	) {
		dto.setId(id);
		ProductDTO data = service.updateProduct(id, dto);
		return SuccessResponseDTO.of(SuccessCode.PRODUCT_UPDATED, data);
	}

	@Operation(summary = "상품 삭제", description = "상품을 soft delete 처리합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 삭제 성공"),
		@ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
	})
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponseDTO<Void> deleteProduct(@PathVariable Long id) {
		service.deleteProduct(id);
		return SuccessResponseDTO.of(SuccessCode.PRODUCT_DELETED);
	}
}
