package com.hana8.hanaro.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.converter.AccountNumConverter;

import io.hypersistence.utils.hibernate.id.Tsid;
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
	@Tsid
	private Long id;

	@Convert(converter = AccountNumConverter.class)
	@Column(nullable = false, length = 255)
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
		foreignKey = @ForeignKey(name = "fk_Account_user")
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ToString.Exclude
	private User user;
}
