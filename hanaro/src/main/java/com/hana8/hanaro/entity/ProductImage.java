package com.hana8.hanaro.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ProductImage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	@ToString.Exclude
	private Product product;

	@Column(nullable = false)
	private String orgName;   // 원본 파일명

	@Column(nullable = false)
	private String saveName;  // 저장 파일명 (UUID 등)

	@Column(nullable = false)
	private String saveDir;   // 저장 경로 (예: 2026/03/14)

	private Integer order;    // 노출 순서

	private String remark;
}
