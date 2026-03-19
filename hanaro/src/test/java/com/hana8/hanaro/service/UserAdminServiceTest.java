package com.hana8.hanaro.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.UserAdminDTO;
import com.hana8.hanaro.dto.UserAdminSearchDTO;
import com.hana8.hanaro.dto.UserAdminSubscriptionDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.mapper.UserAdminMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.SubscriptionRepository;
import com.hana8.hanaro.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

	private UserAdminService userAdminService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private SubscriptionRepository subscriptionRepository;

	private User user;
	private Account basicAccount;
	private Account subAccount;
	private Subscription subscription;

	@BeforeEach
	void setUp() {
		UserAdminMapper userAdminMapper = Mappers.getMapper(UserAdminMapper.class);

		userAdminService = new UserAdminService(
			userRepository,
			accountRepository,
			subscriptionRepository,
			userAdminMapper
		);
		user = new User();
		user.setId(1L);
		user.setEmail("user@test.com");
		user.setName("테스트유저");
		user.setRole(Role.ROLE_USER);

		basicAccount = new Account();
		basicAccount.setId(10L);
		basicAccount.setUser(user);
		basicAccount.setAccountType(AccountType.BASIC);
		basicAccount.setBalance(1000L);

		subAccount = new Account();
		subAccount.setId(20L);
		subAccount.setUser(user);
		subAccount.setAccountType(AccountType.SAVINGS);
		subAccount.setBalance(5000L);

		subscription = new Subscription();
		subscription.setUser(user);
		subscription.setAccount(subAccount);
		subscription.setStatus(AccountStatus.ACTIVE);
		subscription.setMaturityInterest(BigDecimal.valueOf(300));
	}

	@Test
	void getUsers_success() {
		given(userRepository.findAllByRole(Role.ROLE_USER)).willReturn(List.of(user));

		List<UserAdminDTO> result = userAdminService.getUsers();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getEmail()).isEqualTo("user@test.com");
	}

	@Test
	void searchUsers_success() {
		given(userRepository.searchByKeyword("테스트")).willReturn(List.of(user));
		given(subscriptionRepository.findAllByUserIdAndStatus(1L, AccountStatus.ACTIVE))
			.willReturn(List.of(subscription));

		List<UserAdminSearchDTO> result = userAdminService.searchUsers("테스트");

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getEmail()).isEqualTo("user@test.com");
		assertThat(result.getFirst().getSubscriptions()).hasSize(1);
	}

	@Test
	void getUserSubscriptions_success() {
		given(userRepository.existsById(1L)).willReturn(true);
		given(subscriptionRepository.findAllByUserIdAndStatusIn(1L, List.of(AccountStatus.ACTIVE, AccountStatus.MATURED)))
			.willReturn(List.of(subscription));

		List<UserAdminSubscriptionDTO> result = userAdminService.getUserSubscriptions(1L);

		assertThat(result).hasSize(1);
	}

	@Test
	void getUserSubscriptions_fail_whenUserNotFound() {
		given(userRepository.existsById(1L)).willReturn(false);

		assertThatThrownBy(() -> userAdminService.getUserSubscriptions(1L))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException)e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
			});
	}

	@Test
	void processMaturity_success() {
		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.of(subscription));
		given(accountRepository.findByUserIdAndAccountType(1L, AccountType.BASIC))
			.willReturn(Optional.of(basicAccount));
		given(accountRepository.findById(20L)).willReturn(Optional.of(subAccount));

		userAdminService.processMaturity(1L, 20L);

		assertThat(basicAccount.getBalance()).isEqualTo(6300L);
		assertThat(subAccount.getBalance()).isEqualTo(0L);
		assertThat(subscription.getStatus()).isEqualTo(AccountStatus.MATURED);
		assertThat(subscription.getMaturedAt()).isNotNull();

		verify(accountRepository).save(basicAccount);
		verify(accountRepository).save(subAccount);
	}

	@Test
	void processMaturity_success_whenMaturityInterestIsNull() {
		subscription.setMaturityInterest(null);

		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.of(subscription));
		given(accountRepository.findByUserIdAndAccountType(1L, AccountType.BASIC))
			.willReturn(Optional.of(basicAccount));
		given(accountRepository.findById(20L)).willReturn(Optional.of(subAccount));

		userAdminService.processMaturity(1L, 20L);

		assertThat(basicAccount.getBalance()).isEqualTo(6000L);
		assertThat(subAccount.getBalance()).isEqualTo(0L);
		assertThat(subscription.getStatus()).isEqualTo(AccountStatus.MATURED);
	}

	@Test
	void processMaturity_fail_whenSubscriptionNotFound() {
		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> userAdminService.processMaturity(1L, 20L))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException)e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SUBSCRIPTION_NOT_FOUND);
			});
	}

	@Test
	void processMaturity_fail_whenAlreadyMatured() {
		subscription.setStatus(AccountStatus.MATURED);
		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.of(subscription));

		assertThatThrownBy(() -> userAdminService.processMaturity(1L, 20L))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException)e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SUBSCRIPTION_ALREADY_MATURED);
			});
	}

	@Test
	void processMaturity_fail_whenAlreadyCancelled() {
		subscription.setStatus(AccountStatus.CANCELLED);
		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.of(subscription));

		assertThatThrownBy(() -> userAdminService.processMaturity(1L, 20L))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException)e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
			});
	}

	@Test
	void processMaturity_fail_whenBasicAccountNotFound() {
		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.of(subscription));
		given(accountRepository.findByUserIdAndAccountType(1L, AccountType.BASIC))
			.willReturn(Optional.empty());

		assertThatThrownBy(() -> userAdminService.processMaturity(1L, 20L))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException)e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
			});
	}

	@Test
	void processMaturity_fail_whenSubAccountNotFound() {
		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.of(subscription));
		given(accountRepository.findByUserIdAndAccountType(1L, AccountType.BASIC))
			.willReturn(Optional.of(basicAccount));
		given(accountRepository.findById(20L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> userAdminService.processMaturity(1L, 20L))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException)e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
			});
	}
}
