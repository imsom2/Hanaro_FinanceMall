package com.hana8.hanaro.entity;
import com.hana8.hanaro.common.enums.InterestType;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "interest")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Interest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_id", nullable = false)
	@ToString.Exclude
	private Subscription subscription;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InterestType interestType;

	@Column(nullable = false)
	private Long amount;  // 이자 금액
}
