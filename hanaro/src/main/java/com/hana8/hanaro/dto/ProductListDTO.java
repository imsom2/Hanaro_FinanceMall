package com.hana8.hanaro.dto;

import java.math.BigDecimal;

import com.hana8.hanaro.common.enums.ProductType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 목록 조회 응답 DTO")
public class ProductListDTO {

	@Schema(description = "상품 ID", example = "1")
	private Long id;

	@Schema(description = "상품 이름", example = "하나 정기예금")
	private String productName;

	@Schema(description = "상품 타입", allowableValues = {"DEPOSIT", "SAVINGS"})
	private ProductType productType;

	@Schema(description = "만기 금리 (%)", example = "3.50")
	private BigDecimal maturityYield;

	@Schema(description = "가입 기간 (개월)", example = "12")
	private Integer period;

	@Schema(description = "대표 이미지 정보")
	private ProductImageDTO thumbImage;
}
