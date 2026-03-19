package com.hana8.hanaro.repository;

import static com.hana8.hanaro.entity.QSubscription.subscription;

import java.util.List;
import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.entity.Subscription;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class SubscriptionRepositoryCustomImpl implements SubscriptionRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	private JPAQueryFactory queryFactory() {
		return new JPAQueryFactory(entityManager);
	}

	@Override
	public List<Subscription> findAllByUserIdAndStatusIn(Long userId, List<AccountStatus> statuses) {
		return queryFactory()
			.selectFrom(subscription)
			.where(
				subscription.user.id.eq(userId),
				subscription.status.in(statuses)
			)
			.fetch();
	}
}
