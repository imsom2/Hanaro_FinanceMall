package com.hana8.hanaro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.ProductType;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "int unsigned")
	private Long id;

	@Column(nullable = false)
	private String productName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductType productType;

	@Column(nullable = false, columnDefinition = "int unsigned")
	private Long min;  // 최저 납입금액

	@Column(nullable = false, columnDefinition = "int unsigned")
	private Long max;  // 최대 납입금액

	@Column(nullable = false, columnDefinition = "int unsigned")
	private Integer period;  // 가입기간 (개월)

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal maturityYield;  // 만기수익률

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal cancelYield;  // 해지수익률

	@Column(columnDefinition = "TEXT")
	private String description;  // 상품설명

	@Column(nullable = false)
	@Builder.Default
	private boolean isDeleted = false;

	@OneToMany(mappedBy = "product", orphanRemoval = true)
	@ToString.Exclude
	@Builder.Default
	private List<ProductImage> images = new ArrayList<>();
}
