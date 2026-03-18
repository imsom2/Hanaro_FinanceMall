package com.hana8.hanaro.entity;

import java.util.ArrayList;
import java.util.List;

import com.hana8.hanaro.common.enums.Role;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = {
	@UniqueConstraint(
		name = "uniq_User_email",
		columnNames = {"email"}
	)
})
@Builder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

	@Id
	@Tsid
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String passwd;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private Role role = Role.ROLE_USER;

	@OneToMany(mappedBy = "user", orphanRemoval = true)
	@ToString.Exclude
	@Builder.Default
	private List<Account> accounts = new ArrayList<>();

	@OneToMany(mappedBy = "user", orphanRemoval = true)
	@ToString.Exclude
	@Builder.Default
	private List<Subscription> subscriptions = new ArrayList<>();

}
