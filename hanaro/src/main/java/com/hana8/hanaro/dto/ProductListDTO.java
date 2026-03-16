package com.hana8.hanaro.dto;

import java.math.BigDecimal;

import com.hana8.hanaro.common.enums.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListDTO {
	private Long id;
	private String productName;
	private ProductType productType;
	private BigDecimal maturityYield;
	private Integer period;
	private ProductImageDTO thumbImage;
}
