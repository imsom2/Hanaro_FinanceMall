package com.hana8.hanaro.controller;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.ProductDetailDTO;
import com.hana8.hanaro.dto.ProductListDTO;
import com.hana8.hanaro.dto.SubscriptionRequestDTO;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.service.ProductService;
import com.hana8.hanaro.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "상품", description = "예적금 상품 관련 일반 USER용 API입니다")
public class ProductController {

	private final ProductService service;
	private final SubscriptionService subscriptionService;


	@Operation(summary = "상품 가입",
		description = "희망 계좌번호를 등록하여 새로운 상품 전용 계좌를 개설합니다.\n\n계좌번호를 입력하지 않을 경우 랜덤 생성합니다.\n\n납입 주기가 월 단위일 경우 paymentDay, 주 단위일 경우 paymentDayOfWeek만 입력하면 됩니다.  ")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "상품 가입 성공"),
		@ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content),
		@ApiResponse(responseCode = "404", description = "사용자 또는 상품 정보를 찾을 수 없음", content = @Content),
		@ApiResponse(responseCode = "409", description = "계좌번호 중복", content = @Content)
	})
	@PostMapping("/{productId}/subscribe")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponseDTO<Void> subscribe(
		@AuthenticationPrincipal UserDetailsDTO userDetails,
		@PathVariable Long productId,
		@Valid @RequestBody SubscriptionRequestDTO dto
	) {
		subscriptionService.subscribeProduct(userDetails.getId(), productId, dto);
		return SuccessResponseDTO.of(SuccessCode.SUBSCRIPTION_SUCCESS, null);
	}

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
	@GetMapping("/{productId}")
	public SuccessResponseDTO<ProductDetailDTO> getProduct(@PathVariable Long productId) {
		ProductDetailDTO data = service.getProduct(productId);
		return SuccessResponseDTO.of(SuccessCode._OK, data);
	}
}
