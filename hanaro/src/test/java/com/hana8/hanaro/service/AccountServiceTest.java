package com.hana8.hanaro.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.dto.AccountDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.mapper.AccountMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@InjectMocks
	private AccountService accountService;

	@Mock
	private AccountRepository accountRepository;
	@Mock private SubscriptionRepository subscriptionRepository;
	@Mock private InterestCalculator interestCalculator;
	@Mock private AccountMapper accountMapper;

	@Test
	void getAccountDetail_basicAccount_success() {
		Long userId = 1L;
		Long accountId = 10L;

		User user = new User();
		user.setId(userId);

		Account account = new Account();
		account.setId(accountId);
		account.setUser(user);
		account.setAccountType(AccountType.BASIC);

		AccountDTO dto = new AccountDTO();

		given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
		given(accountMapper.toBasicAccountDTO(account)).willReturn(dto);

		AccountDTO result = accountService.getAccountDetail(userId, accountId);

		assertThat(result).isEqualTo(dto);
	}

	@Test
	void deposit_success() {
		Long userId = 1L;
		Long accountId = 10L;

		User user = new User();
		user.setId(userId);

		Account account = new Account();
		account.setUser(user);
		account.setAccountType(AccountType.BASIC);
		account.setBalance(1000L);

		given(accountRepository.findById(accountId)).willReturn(Optional.of(account));

		accountService.deposit(userId, accountId, 500L);

		assertThat(account.getBalance()).isEqualTo(1500L);
		verify(accountRepository).save(account);
	}

	@Test
	void transfer_depositProduct_throw() {
		Subscription sub = new Subscription();

		Product product = new Product();
		product.setProductType(ProductType.DEPOSIT);

		sub.setProduct(product);
		User user = User.builder()
			.id(1L)
			.build();
		sub.setUser(user);
		sub.setStatus(AccountStatus.ACTIVE);

		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.of(sub));

		assertThatThrownBy(() -> accountService.transfer(1L, 1L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void cancel_success() {
		User user = new User();
		user.setId(1L);

		Account basic = new Account();
		basic.setBalance(1000L);

		Account subAccount = new Account();
		subAccount.setBalance(500L);

		Subscription sub = new Subscription();
		sub.setUser(user);
		sub.setAccount(subAccount);
		sub.setStatus(AccountStatus.ACTIVE);

		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.of(sub));
		given(accountRepository.findByUserIdAndAccountType(1L, AccountType.BASIC))
			.willReturn(Optional.of(basic));

		given(interestCalculator.calculateCancelInterest(sub))
			.willReturn(BigDecimal.valueOf(100));
		given(interestCalculator.calculateCurrentBalance(sub))
			.willReturn(500L);

		accountService.cancel(1L, 1L);

		assertThat(basic.getBalance()).isEqualTo(1600L);
		assertThat(subAccount.getBalance()).isEqualTo(0L);
		assertThat(sub.getStatus()).isEqualTo(AccountStatus.CANCELLED);
	}

	// ── getAllAccounts ────────────────────────────────────────────────────

	@Test
	void getAllAccounts_basicAccount_returnsList() {
		User user = new User();
		user.setId(1L);

		Account basic = new Account();
		basic.setId(10L);
		basic.setAccountType(AccountType.BASIC);
		basic.setUser(user);

		AccountDTO dto = new AccountDTO();
		given(accountRepository.findAllByUserId(1L)).willReturn(List.of(basic));
		given(accountMapper.toBasicAccountDTO(basic)).willReturn(dto);

		List<AccountDTO> result = accountService.getAllAccounts(1L);

		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	void getAllAccounts_cancelledSubscription_filtered() {
		User user = new User();
		user.setId(1L);

		Account savingsAccount = new Account();
		savingsAccount.setId(20L);
		savingsAccount.setAccountType(AccountType.SAVINGS);
		savingsAccount.setUser(user);

		Subscription cancelled = new Subscription();
		cancelled.setStatus(AccountStatus.CANCELLED);
		cancelled.setUser(user);

		given(accountRepository.findAllByUserId(1L)).willReturn(List.of(savingsAccount));
		given(subscriptionRepository.findByAccountId(20L)).willReturn(Optional.of(cancelled));

		List<AccountDTO> result = accountService.getAllAccounts(1L);

		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void getAllAccounts_savingsWithActiveSubscription() {
		User user = new User();
		user.setId(1L);

		Account savingsAccount = new Account();
		savingsAccount.setId(30L);
		savingsAccount.setAccountType(AccountType.SAVINGS);
		savingsAccount.setUser(user);

		Product product = new Product();
		product.setProductName("하나 적금");

		Account subAcct = new Account();
		subAcct.setAccountType(AccountType.SAVINGS);

		Subscription active = new Subscription();
		active.setStatus(AccountStatus.ACTIVE);
		active.setUser(user);
		active.setAccount(subAcct);
		active.setProduct(product);

		AccountDTO dto = new AccountDTO();
		dto.setAccountType(AccountType.SAVINGS);

		given(accountRepository.findAllByUserId(1L)).willReturn(List.of(savingsAccount));
		given(subscriptionRepository.findByAccountId(30L)).willReturn(Optional.of(active));
		given(accountMapper.toSubscriptionAccountDTO(active)).willReturn(dto);
		given(interestCalculator.calculateCurrentBalance(active)).willReturn(50000L);
		given(interestCalculator.calculateCancelInterest(active)).willReturn(BigDecimal.valueOf(500));

		List<AccountDTO> result = accountService.getAllAccounts(1L);

		assertThat(result.size()).isEqualTo(1);
	}

	// ── getAccountDetail ─────────────────────────────────────────────────

	@Test
	void getAccountDetail_accountNotFound_throws() {
		given(accountRepository.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.getAccountDetail(1L, 99L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void getAccountDetail_accessDenied_throws() {
		User owner = new User();
		owner.setId(2L); // different from userId=1

		Account account = new Account();
		account.setId(10L);
		account.setUser(owner);
		account.setAccountType(AccountType.BASIC);

		given(accountRepository.findById(10L)).willReturn(Optional.of(account));

		assertThatThrownBy(() -> accountService.getAccountDetail(1L, 10L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void getAccountDetail_subscriptionAccount_success() {
		User user = new User();
		user.setId(1L);

		Account account = new Account();
		account.setId(10L);
		account.setUser(user);
		account.setAccountType(AccountType.SAVINGS);

		Subscription sub = new Subscription();
		AccountDTO dto = new AccountDTO();

		given(accountRepository.findById(10L)).willReturn(Optional.of(account));
		given(subscriptionRepository.findByAccountId(10L)).willReturn(Optional.of(sub));
		given(accountMapper.toSubscriptionAccountDTO(sub)).willReturn(dto);
		given(interestCalculator.calculateCurrentBalance(sub)).willReturn(100000L);
		given(interestCalculator.calculateCancelInterest(sub)).willReturn(BigDecimal.valueOf(1000));

		AccountDTO result = accountService.getAccountDetail(1L, 10L);

		assertThat(result).isEqualTo(dto);
	}

	// ── getValidSubscription error paths ─────────────────────────────────

	@Test
	void transfer_subscriptionNotFound_throws() {
		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.transfer(1L, 1L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void transfer_alreadyCancelled_throws() {
		User user = new User();
		user.setId(1L);

		Subscription sub = new Subscription();
		sub.setUser(user);
		sub.setStatus(AccountStatus.CANCELLED);

		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.of(sub));

		assertThatThrownBy(() -> accountService.transfer(1L, 1L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void transfer_alreadyMatured_throws() {
		User user = new User();
		user.setId(1L);

		Subscription sub = new Subscription();
		sub.setUser(user);
		sub.setStatus(AccountStatus.MATURED);

		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.of(sub));

		assertThatThrownBy(() -> accountService.transfer(1L, 1L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void transfer_monthly_wrongDay_throws() {
		User user = new User();
		user.setId(1L);

		Product product = new Product();
		product.setProductType(ProductType.SAVINGS);

		Subscription sub = new Subscription();
		sub.setUser(user);
		sub.setProduct(product);
		sub.setStatus(AccountStatus.ACTIVE);
		sub.setPaymentCycle(PaymentCycle.MONTHLY);
		sub.setPaymentDay(LocalDate.now().getDayOfMonth() == 1 ? 2 : 1); // 오늘이 아닌 날

		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.of(sub));

		assertThatThrownBy(() -> accountService.transfer(1L, 1L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void transfer_weekly_wrongDay_throws() {
		User user = new User();
		user.setId(1L);

		Product product = new Product();
		product.setProductType(ProductType.SAVINGS);

		// 오늘 요일과 다른 요일 선택
		DayOfWeek today = LocalDate.now().getDayOfWeek();
		DayOfWeek wrongDay = today == DayOfWeek.MONDAY ? DayOfWeek.TUESDAY : DayOfWeek.MONDAY;

		Subscription sub = new Subscription();
		sub.setUser(user);
		sub.setProduct(product);
		sub.setStatus(AccountStatus.ACTIVE);
		sub.setPaymentCycle(PaymentCycle.WEEKLY);
		sub.setPaymentDayOfWeek(wrongDay);

		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.of(sub));

		assertThatThrownBy(() -> accountService.transfer(1L, 1L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void transfer_monthly_insufficientBalance_throws() {
		User user = new User();
		user.setId(1L);

		Product product = new Product();
		product.setProductType(ProductType.SAVINGS);

		Account subAccount = new Account();
		subAccount.setBalance(0L);

		Subscription sub = new Subscription();
		sub.setUser(user);
		sub.setProduct(product);
		sub.setStatus(AccountStatus.ACTIVE);
		sub.setPaymentCycle(PaymentCycle.MONTHLY);
		sub.setPaymentDay(LocalDate.now().getDayOfMonth());
		sub.setPaymentAmount(10000L);
		sub.setAccount(subAccount);

		Account basic = new Account();
		basic.setBalance(5000L); // 잔액 부족

		given(subscriptionRepository.findByAccountId(1L)).willReturn(Optional.of(sub));
		given(accountRepository.findByUserIdAndAccountType(1L, AccountType.BASIC))
			.willReturn(Optional.of(basic));

		assertThatThrownBy(() -> accountService.transfer(1L, 1L))
			.isInstanceOf(BusinessException.class);
	}

	// ── deposit error paths ───────────────────────────────────────────────

	@Test
	void deposit_accountNotFound_throws() {
		given(accountRepository.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.deposit(1L, 99L, 1000L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void deposit_accessDenied_throws() {
		User owner = new User();
		owner.setId(2L);

		Account account = new Account();
		account.setUser(owner);
		account.setAccountType(AccountType.BASIC);

		given(accountRepository.findById(10L)).willReturn(Optional.of(account));

		assertThatThrownBy(() -> accountService.deposit(1L, 10L, 1000L))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void deposit_notBasicType_throws() {
		User user = new User();
		user.setId(1L);

		Account account = new Account();
		account.setUser(user);
		account.setAccountType(AccountType.SAVINGS); // not BASIC

		given(accountRepository.findById(10L)).willReturn(Optional.of(account));

		assertThatThrownBy(() -> accountService.deposit(1L, 10L, 1000L))
			.isInstanceOf(BusinessException.class);
	}

	// ── resolveAccountNum ─────────────────────────────────────────────────

	@Test
	void resolveAccountNum_withUniqueWishNum_returnsWishNum() {
		String wishNum = "12345678901";
		given(accountRepository.existsByAccountNum(AccountUtil.encrypt(wishNum))).willReturn(false);

		String result = accountService.resolveAccountNum(wishNum);

		assertThat(result).isEqualTo(wishNum);
	}

	@Test
	void resolveAccountNum_withDuplicateWishNum_throws() {
		String wishNum = "12345678901";
		given(accountRepository.existsByAccountNum(AccountUtil.encrypt(wishNum))).willReturn(true);

		assertThatThrownBy(() -> accountService.resolveAccountNum(wishNum))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void resolveAccountNum_withNull_autoGenerates() {
		given(accountRepository.existsByAccountNum(any())).willReturn(false);

		String result = accountService.resolveAccountNum(null);

		assertThat(result).isNotNull();
		assertThat(result.length()).isEqualTo(11);
	}

	@Test
	void resolveAccountNum_withBlank_autoGenerates() {
		given(accountRepository.existsByAccountNum(any())).willReturn(false);

		String result = accountService.resolveAccountNum("   ");

		assertThat(result).isNotNull();
		assertThat(result.length()).isEqualTo(11);
	}
}
