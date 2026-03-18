package com.hana8.hanaro.controller.user;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.ProductDTO;
import com.hana8.hanaro.dto.ProductListDTO;
import com.hana8.hanaro.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "상품", description = "예적금 상품 관련 일반 USER용 API입니다")
public class ProductController {

	private final ProductService service;

	@Operation(summary = "상품 목록 조회", description = "삭제되지 않은 예적금 상품 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
	@GetMapping
	public SuccessResponseDTO<List<ProductListDTO>> getProducts() {
		List<ProductListDTO> data = service.getProducts();
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
}
