package com.hana8.hanaro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
	List<Account> findAllByUserId(Long userId);
	boolean existsByAccountNum(String accountNum);
	Optional<Account> findByUserIdAndAccountType(Long userId, AccountType accountType);
}
