package com.hana8.hanaro.repository;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.entity.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class SubscriptionRepositoryTest {

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AccountRepository accountRepository;

	private User saveUser(String email) {
		return userRepository.save(
			User.builder()
				.email(email)
				.passwd("123")
				.name("테스터")
				.build()
		);
	}

	private Product saveProduct(String productName) {
		return productRepository.save(
			Product.builder()
				.productName(productName)
				.productType(ProductType.SAVINGS)
				.min(1000L)
				.max(100000L)
				.period(12)
				.maturityYield(new BigDecimal("5.0"))
				.cancelYield(new BigDecimal("2.0"))
				.build()
		);
	}

	private Account saveAccount(User user, String accountNum, AccountType accountType) {
		return accountRepository.save(
			Account.builder()
				.accountNum(AccountUtil.encrypt(accountNum))
				.accountType(accountType)
				.user(user)
				.balance(0L)
				.build()
		);
	}

	@Test
	void findByAccountIdTest() {
		User user = saveUser("sub@test.com");
		Product product = saveProduct("하나 적금");
		Account account = saveAccount(user, "99999999999", AccountType.SAVINGS);

		Subscription savedSub = subscriptionRepository.save(
			Subscription.builder()
				.user(user)
				.product(product)
				.account(account)
				.status(AccountStatus.ACTIVE)
				.paymentAmount(10000L)
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);

		Optional<Subscription> result = subscriptionRepository.findByAccountId(account.getId());

		assertThat(result).isPresent();
		assertThat(result.get().getId()).isEqualTo(savedSub.getId());
		assertThat(result.get().getPaymentAmount()).isEqualTo(10000L);
	}

	@Test
	void findAllByUserIdAndStatusTest() {
		User user = saveUser("sub2@test.com");
		Product product = saveProduct("하나 적금");

		Account activeAccount = saveAccount(user, "88888888888", AccountType.SAVINGS);
		Account cancelledAccount = saveAccount(user, "77777777777", AccountType.SAVINGS);

		subscriptionRepository.save(
			Subscription.builder()
				.user(user)
				.product(product)
				.account(activeAccount)
				.status(AccountStatus.ACTIVE)
				.paymentAmount(5000L)
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);

		subscriptionRepository.save(
			Subscription.builder()
				.user(user)
				.product(product)
				.account(cancelledAccount)
				.status(AccountStatus.CANCELLED)
				.paymentAmount(5000L)
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);

		List<Subscription> activeList =
			subscriptionRepository.findAllByUserIdAndStatus(user.getId(), AccountStatus.ACTIVE);

		List<Subscription> cancelledList =
			subscriptionRepository.findAllByUserIdAndStatus(user.getId(), AccountStatus.CANCELLED);

		assertThat(activeList).isNotEmpty();
		assertThat(activeList)
			.extracting(Subscription::getStatus)
			.contains(AccountStatus.ACTIVE);

		assertThat(cancelledList).isNotEmpty();
		assertThat(cancelledList)
			.extracting(Subscription::getStatus)
			.contains(AccountStatus.CANCELLED);
	}

	@Test
	void findAllByUserIdAndStatusInTest() {
		User user = saveUser("statusin@test.com");
		Product product = saveProduct("하나 예금");

		Account activeAccount = saveAccount(user, "11122233344", AccountType.SAVINGS);
		Account maturedAccount = saveAccount(user, "22233344455", AccountType.SAVINGS);
		Account cancelledAccount = saveAccount(user, "33344455566", AccountType.SAVINGS);

		subscriptionRepository.save(
			Subscription.builder()
				.user(user).product(product).account(activeAccount)
				.status(AccountStatus.ACTIVE)
				.paymentAmount(5000L)
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);
		subscriptionRepository.save(
			Subscription.builder()
				.user(user).product(product).account(maturedAccount)
				.status(AccountStatus.MATURED)
				.paymentAmount(5000L)
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);
		subscriptionRepository.save(
			Subscription.builder()
				.user(user).product(product).account(cancelledAccount)
				.status(AccountStatus.CANCELLED)
				.paymentAmount(5000L)
				.joinDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(12))
				.build()
		);

		List<Subscription> result = subscriptionRepository.findAllByUserIdAndStatusIn(
			user.getId(), List.of(AccountStatus.ACTIVE, AccountStatus.MATURED)
		);

		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(Subscription::getStatus)
			.containsExactlyInAnyOrder(AccountStatus.ACTIVE, AccountStatus.MATURED);
	}

}
