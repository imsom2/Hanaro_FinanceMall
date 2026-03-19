package com.hana8.hanaro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryCustom {
	Optional<Subscription> findByAccountId(Long accountId);
	List<Subscription> findAllByUserIdAndStatus(Long userId, AccountStatus status);
}
