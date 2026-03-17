package com.hana8.hanaro.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hana8.hanaro.common.enums.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

	private Long id;
	private String productName;
	private ProductType productType;
	private Long min;
	private Long max;
	private Integer period;
	private BigDecimal maturityYield;
	private BigDecimal cancelYield;
	private String description;

	@Builder.Default
	private List<ProductImageDTO> images = new ArrayList<>();
}
