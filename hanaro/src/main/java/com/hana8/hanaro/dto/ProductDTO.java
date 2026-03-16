package com.hana8.hanaro.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hana8.hanaro.common.enums.ProductType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

	@NotNull(groups = OnUpdate.class, message = "수정할 상품의 id를 입력하세요!")
	private Long id;

	@NotBlank(message = "상품명은 필수입니다.")
	@Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
	private String productName;

	@NotNull(message = "상품 종류는 필수입니다.")
	private ProductType productType;

	@NotNull(message = "최소 금액은 필수입니다.")
	@Positive(message = "최소 금액은 0보다 커야 합니다.")
	private Long min;

	@NotNull(message = "최대 금액은 필수입니다.")
	@Positive(message = "최대 금액은 0보다 커야 합니다.")
	private Long max;

	@NotNull(message = "가입 기간은 필수입니다.")
	@Positive(message = "가입 기간은 0보다 커야 합니다.")
	private Integer period;

	@NotNull(message = "만기 수익률은 필수입니다.")
	@DecimalMin(value = "0.0", message = "만기 수익률은 0 이상이어야 합니다.")
	private BigDecimal maturityYield;

	@NotNull(message = "해지 수익률은 필수입니다.")
	@DecimalMin(value = "0.0", message = "해지 수익률은 0 이상이어야 합니다.")
	private BigDecimal cancelYield;

	@Size(max = 1000, message = "상품 설명은 1000자 이하여야 합니다.")
	private String description;

	@Builder.Default
	private List<ProductImageDTO> images = new ArrayList<>();

	@AssertTrue(message = "최소 금액은 최대 금액보다 클 수 없습니다.")
	public boolean isValidAmountRange() {
		if (min == null || max == null) return true;
		return min <= max;
	}

	public interface OnCreate {
	}

	public interface OnUpdate {
	}
}
