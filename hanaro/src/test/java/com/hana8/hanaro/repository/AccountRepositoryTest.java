package com.hana8.hanaro.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class AccountRepositoryTest {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private UserRepository userRepository;

	private User saveUser(String email) {
		return userRepository.save(
			User.builder()
				.email(email)
				.passwd("password123")
				.name("김하나")
				.build()
		);
	}

	private void saveAccount(User user, String accountNum, AccountType type) {
		accountRepository.save(
			Account.builder()
				.accountNum(AccountUtil.encrypt(accountNum))
				.accountType(type)
				.user(user)
				.build()
		);
	}

	@Test
	void findAllByUserId() {
		User user = saveUser("test@hana.com");

		saveAccount(user, "11111111111", AccountType.BASIC);
		saveAccount(user, "22222222222", AccountType.SAVINGS);

		List<Account> result = accountRepository.findAllByUserId(user.getId());

		assertThat(result).hasSize(2);
	}

	@Test
	void existsByAccountNum() {
		User user = saveUser("exists@test.com");

		String rawNum = "12345678901";
		String encryptedNum = AccountUtil.encrypt(rawNum);

		accountRepository.save(
			Account.builder()
				.accountNum(encryptedNum)
				.accountType(AccountType.BASIC)
				.user(user)
				.build()
		);

		assertThat(accountRepository.existsByAccountNum(encryptedNum)).isTrue();
		assertThat(accountRepository.existsByAccountNum(AccountUtil.encrypt("00000000000"))).isFalse();
	}
	@Test
	void findByUserIdAndAccountType() {
		User user = saveUser("type@test.com");

		saveAccount(user, "10000111111", AccountType.BASIC);

		Optional<Account> found =
			accountRepository.findByUserIdAndAccountType(user.getId(), AccountType.BASIC);

		assertThat(found).isPresent();
		assertThat(found.get().getAccountType()).isEqualTo(AccountType.BASIC);
	}

}
