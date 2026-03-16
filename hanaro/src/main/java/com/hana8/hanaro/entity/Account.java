package com.hana8.hanaro.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hana8.hanaro.common.enums.AccountType;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(
			name = "uniq_Account_accountNum",
			columnNames = {"accountNum"}
		)
	})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Account extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "int unsigned")
	private Long id;

	@Column(nullable = false, length = 11)
	private String accountNum;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountType accountType;

	@Column(nullable = false)
	@Builder.Default
	private Long balance = 0L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "user", referencedColumnName = "id",
		columnDefinition = "int unsigned",
		foreignKey = @ForeignKey(name = "fk_Account_user")
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ToString.Exclude
	private User user;
}
