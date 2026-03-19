package com.hana8.hanaro.repository;

import java.util.List;

import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.entity.Subscription;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubscriptionRepositoryCustomImpl implements SubscriptionRepositoryCustom {

	private final EntityManager em;

	@Override
	public List<Subscription> findAllByUserIdAndStatusIn(Long userId, List<AccountStatus> statuses) {
		return em.createQuery(
				"SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status IN :statuses",
				Subscription.class)
			.setParameter("userId", userId)
			.setParameter("statuses", statuses)
			.getResultList();
	}
}
