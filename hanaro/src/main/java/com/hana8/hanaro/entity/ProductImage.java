package com.hana8.hanaro.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

	@Column(nullable = false)
	private String orgName;   // 원본 파일명

	@Column(nullable = false)
	private String saveName;  // 저장 파일명

	@Column(nullable = false)
	private String saveDir;   // 저장 경로

	private Integer sortOrder;    // 노출 순서

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "product", referencedColumnName = "id",
		columnDefinition = "int unsigned",
		foreignKey = @ForeignKey(name = "fk_ProductImage_product"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Product product;
}
