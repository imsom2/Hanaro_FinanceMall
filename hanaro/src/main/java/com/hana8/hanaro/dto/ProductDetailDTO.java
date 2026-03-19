package com.hana8.hanaro.dto;

import java.math.BigDecimal;
import java.util.List;

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
@Schema(description = "상품 상세 조회 응답 DTO")
public class ProductDetailDTO {

	@Schema(description = "상품 ID", example = "1")
	private Long id;

	@Schema(description = "상품명", example = "하나 정기예금")
	private String productName;

	@Schema(description = "상품 타입", allowableValues = {"DEPOSIT", "SAVINGS"})
	private ProductType productType;

	@Schema(description = "최소 납입 금액(원)", example = "10000")
	private Long min;

	@Schema(description = "최대 납입 금액(원)", example = "5000000")
	private Long max;

	@Schema(description = "가입 기간(개월)", example = "12")
	private Integer period;

	@Schema(description = "만기 금리(%)", example = "3.50")
	private BigDecimal maturityYield;

	@Schema(description = "중도 해지 금리(%)", example = "1.00")
	private BigDecimal cancelYield;

	@Schema(description = "상품 상세 설명", example = "1년 만기 기준 우대금리를 제공하는 정기예금 상품입니다.")
	private String description;

	@Schema(description = "상품 이미지 목록")
	private List<ProductImageDTO> images;

}
