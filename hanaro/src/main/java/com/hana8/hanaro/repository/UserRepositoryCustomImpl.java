package com.hana8.hanaro.repository;

import static com.hana8.hanaro.entity.QUser.user;
import static com.hana8.hanaro.entity.QAccount.account;

import java.util.List;
import java.util.Optional;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	private JPAQueryFactory queryFactory() {
		return new JPAQueryFactory(entityManager);
	}

	@Override
	public Optional<User> findByEmailWithBasicAccount(String email) {
		User result = queryFactory()
			.selectFrom(user)
			.join(user.accounts, account).fetchJoin()
			.where(
				user.email.eq(email),
				account.accountType.eq(AccountType.BASIC)
			)
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public List<User> searchByKeyword(String keyword) {
		return queryFactory()
			.selectFrom(user)
			.where(
				user.name.contains(keyword)
					.or(user.email.contains(keyword))
			)
			.fetch();
	}
}
