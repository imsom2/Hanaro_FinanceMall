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
@Table(name = "product")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductType productType;

	private Long max;  // 최대 납입금액

	@Enumerated(EnumType.STRING)
	private PaymentCycle paymentCycle;  // 납입주기

	private Integer period;  // 가입기간 (개월)

	@Column(precision = 5, scale = 2)
	private BigDecimal maturityYield;  // 만기수익률

	@Column(precision = 5, scale = 2)
	private BigDecimal cancelYield;  // 해지수익률

	@Column(columnDefinition = "TEXT")
	private String description;  // 상품설명

	@Column(nullable = false)
	private boolean isDeleted = false;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<ProductImage> images = new ArrayList<>();

	// 편의 메서드
	public void addImage(ProductImage image) {
		images.add(image);
		image.setProduct(this);
	}

	public void softDelete() {
		this.isDeleted = true;
	}
}
