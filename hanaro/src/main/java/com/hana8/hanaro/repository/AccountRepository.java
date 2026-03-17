package com.hana8.hanaro.repository;

import com.hana8.hanaro.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

	boolean existsByAccountNum(String accountNum);

	List<Account> findByUserId(Long userId);
}
