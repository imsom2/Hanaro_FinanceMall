package com.hana8.hanaro.entity;

import com.hana8.hanaro.common.enums.AccountType;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "account")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Account extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@ToString.Exclude
	private User user;

	@Column(nullable = false, unique = true)
	private String accountNum;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountType accountType;

	@Column(nullable = false)
	private Long balance = 0L;

	// 편의 메서드
	public void deposit(Long amount) {
		this.balance += amount;
	}

	public void withdraw(Long amount) {
		if (this.balance < amount) throw new IllegalStateException("잔액이 부족합니다.");
		this.balance -= amount;
	}
}
