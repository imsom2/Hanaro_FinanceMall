package com.hana8.hanaro.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;

import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private EntityManager entityManager;

	private User saveUser(String email, String name, Role role) {
		return userRepository.save(
			User.builder()
				.email(email)
				.passwd("1234")
				.name(name)
				.role(role)
				.build()
		);
	}

	@Test
	void existsByEmail() {
		saveUser("test@hana.com", "하나", Role.ROLE_USER);

		assertThat(userRepository.existsByEmail("test@hana.com")).isTrue();
		assertThat(userRepository.existsByEmail("none@hana.com")).isFalse();
	}

	@Test
	void findAllByRole() {
		saveUser("u1@test.com", "유저1", Role.ROLE_USER);
		saveUser("admin@test.com", "관리자", Role.ROLE_ADMIN);

		List<User> users = userRepository.findAllByRole(Role.ROLE_USER);
		List<User> admins = userRepository.findAllByRole(Role.ROLE_ADMIN);

		assertThat(users).extracting(User::getEmail).contains("u1@test.com");
		assertThat(admins).extracting(User::getEmail).contains("admin@test.com");
	}

	@Test
	void findByEmail() {
		saveUser("find@test.com", "조회용", Role.ROLE_USER);

		Optional<User> found = userRepository.findByEmail("find@test.com");
		Optional<User> notFound = userRepository.findByEmail("wrong@test.com");

		assertThat(found).isPresent();
		assertThat(found.get().getEmail()).isEqualTo("find@test.com");
		assertThat(notFound).isEmpty();
	}

	@Test
	void searchByKeyword() {
		saveUser("hana_unique@test.com", "김하나유니크", Role.ROLE_USER);
		saveUser("naro_unique@test.com", "최나로유니크", Role.ROLE_USER);

		List<User> searchByName = userRepository.searchByKeyword("하나유니크");
		List<User> searchByEmail = userRepository.searchByKeyword("unique@test.com");
		List<User> noResult = userRepository.searchByKeyword("없는사람");

		assertThat(searchByName)
			.extracting(User::getName)
			.contains("김하나유니크");

		assertThat(searchByEmail)
			.extracting(User::getEmail)
			.contains("hana_unique@test.com", "naro_unique@test.com");

		assertThat(noResult).isEmpty();
	}

	@Test
	void findByEmailWithBasicAccount_withAccount() {
		User user = saveUser("login@test.com", "로그인유저", Role.ROLE_USER);

		accountRepository.save(
			Account.builder()
				.accountNum(AccountUtil.encrypt("11100000001"))
				.accountType(AccountType.BASIC)
				.user(user)
				.build()
		);

		entityManager.flush();
		entityManager.clear();

		Optional<User> result = userRepository.findByEmailWithBasicAccount("login@test.com");

		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo("login@test.com");
		assertThat(result.get().getAccounts())
			.extracting(Account::getAccountType)
			.containsExactly(AccountType.BASIC);
	}

	@Test
	void findByEmailWithBasicAccount_notFound() {
		Optional<User> result = userRepository.findByEmailWithBasicAccount("notexist@test.com");

		assertThat(result).isEmpty();
	}

}
