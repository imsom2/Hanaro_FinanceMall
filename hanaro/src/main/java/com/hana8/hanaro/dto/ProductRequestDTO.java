package com.hana8.hanaro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hana8.hanaro.common.enums.ProductType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 등록/수정 요청 DTO")
public class ProductRequestDTO {

	@Schema(description = "상품명", example = "하나로 외화 적금 (MZ)")
	@NotBlank(message = "상품명은 필수입니다.")
	@Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
	private String productName;

	@Schema(description = "상품 종류", example = "SAVINGS")
	@NotNull(message = "상품 종류는 필수입니다.")
	private ProductType productType;

	@Schema(description = "최소 가입 금액", example = "10000")
	@NotNull(message = "최소 금액은 필수입니다.")
	@Positive(message = "최소 금액은 0보다 커야 합니다.")
	private Long min;

	@Schema(description = "최대 가입 금액", example = "5000000")
	@NotNull(message = "최대 금액은 필수입니다.")
	@Positive(message = "최대 금액은 0보다 커야 합니다.")
	private Long max;

	@Schema(description = "가입 기간 (개월)", example = "12")
	@NotNull(message = "가입 기간은 필수입니다.")
	@Positive(message = "가입 기간은 0보다 커야 합니다.")
	private Integer period;

	@Schema(description = "만기 수익률 (%)", example = "4.5")
	@NotNull(message = "만기 수익률은 필수입니다.")
	@DecimalMin(value = "0.0", message = "만기 수익률은 0 이상이어야 합니다.")
	private BigDecimal maturityYield;

	@Schema(description = "해지 수익률 (%)", example = "1.5")
	@NotNull(message = "해지 수익률은 필수입니다.")
	@DecimalMin(value = "0.0", message = "해지 수익률은 0 이상이어야 합니다.")
	private BigDecimal cancelYield;

	@Schema(description = "상품 상세 설명", example = "외국인 MZ 세대를 위한 맞춤형 고금리 적금 상품입니다.")
	@Size(max = 1000, message = "상품 설명은 1000자 이하여야 합니다.")
	private String description;

	@Builder.Default
	private List<ProductImageDTO> images = new ArrayList<>();

	@AssertTrue(message = "최소 금액은 최대 금액보다 클 수 없습니다.")
	@Schema(hidden = true) // Swagger 문서 결과값에는 표시되지 않도록 설정
	public boolean isValidAmountRange() {
		if (min == null || max == null) return true;
		return min <= max;
	}

	public interface OnCreate extends Default {
	}

	public interface OnUpdate extends Default {
	}
}
