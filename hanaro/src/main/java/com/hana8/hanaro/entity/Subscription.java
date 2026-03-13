package com.hana8.hanaro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.SubStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "subscription")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Subscription extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	@ToString.Exclude
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	@ToString.Exclude
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	@ToString.Exclude
	private User member;

	@Column(nullable = false)
	private Long paymentAmount;  // 납입금액

	@Enumerated(EnumType.STRING)
	private PaymentCycle paymentCycle;  // 납입주기 (가입 시 지정)

	@Column(nullable = false)
	private LocalDate joinDate;  // 가입일

	@Column(nullable = false)
	private LocalDate endDate;   // 만기일

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubStatus status;

	private LocalDateTime maturedAt;    // 만기처리시각
	private LocalDateTime cancelledAt;  // 해지처리시각

	// 편의 메서드
	public void mature() {
		this.status = SubStatus.MATURED;
		this.maturedAt = LocalDateTime.now();
	}

	public void cancel() {
		this.status = SubStatus.CANCELLED;
		this.cancelledAt = LocalDateTime.now();
	}
}
