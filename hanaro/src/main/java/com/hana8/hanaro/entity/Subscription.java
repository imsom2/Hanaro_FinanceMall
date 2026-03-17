package com.hana8.hanaro.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.SubStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Subscription extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "int unsigned")
	private Long id;

	@Column(nullable = false)
	private Long paymentAmount;  // 납입금액

	@Enumerated(EnumType.STRING)
	private PaymentCycle paymentCycle;  // 납입주기 (가입 시 지정)

	@Column
	private Integer paymentDay;  // MONTHLY일 경우

	@Enumerated(EnumType.STRING)
	private DayOfWeek paymentDayOfWeek;  // WEEKLY일 경우

	@Column(nullable = false)
	private LocalDate joinDate;  // 가입일

	@Column(nullable = false)
	private LocalDate endDate;   // 만기일

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private SubStatus status = SubStatus.ACTIVE;

	@Column(precision = 15, scale = 2)
	private BigDecimal maturityInterest;  // 만기 이자

	private LocalDateTime maturedAt;    // 만기처리시각
	private LocalDateTime cancelledAt;  // 해지처리시각

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "product", referencedColumnName = "id",
		columnDefinition = "int unsigned",
		foreignKey = @ForeignKey(name = "fk_Subscription_product")
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ToString.Exclude
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "account", referencedColumnName = "id",
		foreignKey = @ForeignKey(name = "fk_Subscription_account")
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ToString.Exclude
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "user", referencedColumnName = "id",
		foreignKey = @ForeignKey(name = "fk_Subscription_user")
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ToString.Exclude
	private User user;

}
