package com.hana8.hanaro.entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Interest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "subscription", referencedColumnName = "id",
		columnDefinition = "int unsigned",
		foreignKey = @ForeignKey(name = "fk_Interest_subscription")
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Subscription subscription;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;  // 이자 금액

	@Column(nullable = false)
	private LocalDateTime calcDate;  // 계산 기준일 (배치 실행일)

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal appliedRate;  // 적용된 이율

	@Column(nullable = false, columnDefinition = "int unsigned")
	private Integer elapsedDays;  // 가입일 ~ 계산 기준일
}
