package com.hana8.hanaro.controller.admin;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.ProductDTO;
import com.hana8.hanaro.dto.ProductRequestDTO;
import com.hana8.hanaro.mapper.ProductMapper;
import com.hana8.hanaro.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@Tag(name = "관리자 - 상품 운영", description = "상품을 관리하는 관리자용 API입니다")
public class ProductAdminController {

	private final ProductService service;
	private final ProductMapper mapper;

	@Operation(summary = "상품 등록", description = "새 예적금 상품을 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 등록 성공"),
		@ApiResponse(responseCode = "400", description = "입력값 오류"),
		@ApiResponse(responseCode = "409", description = "중복 데이터")
	})
	@PostMapping
	public SuccessResponseDTO<ProductDTO> createProduct(
		@Validated(ProductRequestDTO.OnCreate.class) @RequestBody ProductRequestDTO requestDto
	) {
		ProductDTO dto = mapper.toDTO(requestDto);
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
	public SuccessResponseDTO<ProductDTO> updateProduct(
		@PathVariable Long id,
		@Validated(ProductRequestDTO.OnUpdate.class) @RequestBody ProductRequestDTO requestDto
	) {
		ProductDTO dto = mapper.toDTO(requestDto);
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
	public SuccessResponseDTO<Void> deleteProduct(@PathVariable Long id) {
		service.deleteProduct(id);
		return SuccessResponseDTO.of(SuccessCode.PRODUCT_DELETED);
	}
}
