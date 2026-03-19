package com.hana8.hanaro.repository;

import java.util.List;

import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.entity.Subscription;

public interface SubscriptionRepositoryCustom {
	List<Subscription> findAllByUserIdAndStatusIn(Long userId, List<AccountStatus> statuses);
}
