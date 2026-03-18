package com.hana8.hanaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hana8.hanaro.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {}
